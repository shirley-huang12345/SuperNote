#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>

#include "com_asus_supernote_pdfconverter_PdfConverter.h"


#define INDEX_USE_PPDF	(-1)

static void Jpeg2PDF_SetXREF(PJPEG2PDF pPDF, int index, int offset, char c) {
	if(INDEX_USE_PPDF == index) index = pPDF->pdfObj;

	if('f' == c)
		sprintf(pPDF->pdfXREF[index], "%010d 65535 f\r\n", offset);
	else
		sprintf(pPDF->pdfXREF[index], "%010d 00000 %c\r\n", offset, c);

	if(JPEG2PDF_DEBUG) logMsg("pPDF->pdfXREF[%d] = %s", index, (int)pPDF->pdfXREF[index], 3,4,5,6);
}

PJPEG2PDF Jpeg2PDF_BeginDocument(double pdfW, double pdfH) {
	PJPEG2PDF pPDF;

	pPDF = (PJPEG2PDF)malloc(sizeof(JPEG2PDF));
	if(pPDF) {
		memset(pPDF, 0, sizeof(JPEG2PDF));
		if(JPEG2PDF_DEBUG) logMsg("PDF List Inited (pPDF = %p)\n", (int)pPDF, 2,3,4,5,6);
		pPDF->pageW = (UINT32)(pdfW * PDF_DOT_PER_INCH);
		pPDF->pageH = (UINT32)(pdfH * PDF_DOT_PER_INCH);
		if(JPEG2PDF_DEBUG) logMsg("PDF Page Size (%d %d)\n", pPDF->pageW, pPDF->pageH,3,4,5,6);

		pPDF->currentOffSet = 0;
		Jpeg2PDF_SetXREF(pPDF, 0, pPDF->currentOffSet, 'f');
		pPDF->currentOffSet += sprintf(pPDF->pdfHeader, "%%PDF-1.3\r\n%%%cJpeg2PDF Engine By: HH [ihaohu@gmail.com]%c\r\n", 0xFF, 0xFF);
		if(JPEG2PDF_DEBUG) logMsg("[pPDF=%p], header: %s", (int)pPDF, (int)pPDF->pdfHeader,3,4,5,6);

		pPDF->imgObj = 0;
		pPDF->pdfObj = 2;		/* 0 & 1 was reserved for xref & document Root */
	}

	return pPDF;
}

STATUS Jpeg2PDF_AddJpeg(PJPEG2PDF pPDF, UINT32 imgW, UINT32 imgH, UINT32 fileSize, UINT8 *pJpeg, UINT8 isColor) {
	STATUS result = ERROR;
	PJPEG2PDF_NODE pNode;

	if(pPDF) {
		if(pPDF->nodeCount >= MAX_PDF_PAGES) {
			logMsg("Add JPEG into PDF Skipped. Reason: Reached Max Page Number (%d) in single PDF.\n", MAX_PDF_PAGES,2,3,4,5,6);
			return result;
		}

		pNode = (PJPEG2PDF_NODE)malloc(sizeof(JPEG2PDF_NODE));
		if(pNode) {
			UINT32 nChars, currentImageObject;
			UINT8 *pFormat, lenStr[256];
			pNode->JpegW = imgW;
			pNode->JpegH = imgH;
			pNode->JpegSize = fileSize;
			pNode->pJpeg = (UINT8 *)malloc(pNode->JpegSize);
			pNode->pNext = NULL;

			if(pNode->pJpeg != NULL) {
				memcpy(pNode->pJpeg, pJpeg, pNode->JpegSize);

				/* Image Object */
				Jpeg2PDF_SetXREF(pPDF, INDEX_USE_PPDF, pPDF->currentOffSet, 'n');
				currentImageObject = pPDF->pdfObj;


				pPDF->currentOffSet += sprintf(pNode->preFormat, "\r\n%d 0 obj\r\n<</Type/XObject/Subtype/Image/Filter/DCTDecode/BitsPerComponent 8/ColorSpace/%s/Width %d/Height %d/Length %d>>\r\nstream\r\n",
					pPDF->pdfObj, ((isColor)? "DeviceRGB" : "DeviceGray"), pNode->JpegW, pNode->JpegH, pNode->JpegSize);

				pPDF->currentOffSet += pNode->JpegSize;

				pFormat = pNode->pstFormat;
				nChars = sprintf(pFormat, "\r\nendstream\r\nendobj\r\n");
				pPDF->currentOffSet += nChars;	pFormat += nChars;
				pPDF->pdfObj++;

				/* Page Object */
				Jpeg2PDF_SetXREF(pPDF, INDEX_USE_PPDF, pPDF->currentOffSet, 'n');
				pNode->PageObj = pPDF->pdfObj;
				nChars = sprintf(pFormat, "%d 0 obj\r\n<</Type/Page/Parent 1 0 R/MediaBox[0 0 %d %d]/Contents %d 0 R/Resources %d 0 R>>\r\nendobj\r\n",
						pPDF->pdfObj, pPDF->pageW, pPDF->pageH, pPDF->pdfObj+1, pPDF->pdfObj + 3);
				pPDF->currentOffSet += nChars;	pFormat += nChars;
				pPDF->pdfObj++;

				/* Contents Object in Page Object */
				Jpeg2PDF_SetXREF(pPDF, INDEX_USE_PPDF, pPDF->currentOffSet, 'n');
				sprintf(lenStr, "q\r\n1 0 0 1 %.2f %.2f cm\r\n%.2f 0 0 %.2f 0 0 cm\r\n/I%d Do\r\nQ\r\n",
						PDF_LEFT_MARGIN, PDF_TOP_MARGIN, pPDF->pageW - 2*PDF_LEFT_MARGIN, pPDF->pageH - 2*PDF_TOP_MARGIN, pPDF->imgObj);
				nChars = sprintf(pFormat, "%d 0 obj\r\n<</Length %d 0 R>>stream\r\n%sendstream\r\nendobj\r\n",
						pPDF->pdfObj, pPDF->pdfObj+1, lenStr);
				pPDF->currentOffSet += nChars;	pFormat += nChars;
				pPDF->pdfObj++;

				/* Length Object in Contents Object */
				Jpeg2PDF_SetXREF(pPDF, INDEX_USE_PPDF, pPDF->currentOffSet, 'n');
				nChars = sprintf(pFormat, "%d 0 obj\r\n%ld\r\nendobj\r\n", pPDF->pdfObj, strlen(lenStr));
				pPDF->currentOffSet += nChars;	pFormat += nChars;
				pPDF->pdfObj++;

				/* Resources Object in Page Object */
				Jpeg2PDF_SetXREF(pPDF, INDEX_USE_PPDF, pPDF->currentOffSet, 'n');
				nChars = sprintf(pFormat, "%d 0 obj\r\n<</ProcSet[/PDF/%s]/XObject<</I%d %d 0 R>>>>\r\nendobj\r\n",
								pPDF->pdfObj, ((isColor)? "ImageC" : "ImageB"), pPDF->imgObj, currentImageObject);
				pPDF->currentOffSet += nChars;	pFormat += nChars;
				pPDF->pdfObj++;

				pPDF->imgObj++;

				/* Update the Link List */
				pPDF->nodeCount++;
				if(1 == pPDF->nodeCount) {
					pPDF->pFirstNode = pNode;
				} else {
					pPDF->pLastNode->pNext = pNode;
				}

				pPDF->pLastNode = pNode;

				result = OK;
			} /* pNode->pJpeg allocated OK */
		} /* pNode is valid */
	} /* pPDF is valid */

	return result;
}

UINT32 Jpeg2PDF_EndDocument(PJPEG2PDF pPDF) {
	UINT32 headerSize, tailerSize, pdfSize = 0;

	if(pPDF) {
		UINT8 strKids[MAX_PDF_PAGES * MAX_KIDS_STRLEN], *pTail = pPDF->pdfTailer;
		UINT32 i, nChars, xrefOffSet;
		PJPEG2PDF_NODE pNode;

		/* Catalog Object. This is the Last Object */
		Jpeg2PDF_SetXREF(pPDF, INDEX_USE_PPDF, pPDF->currentOffSet, 'n');
		nChars = sprintf(pTail, "%d 0 obj\r\n<</Type/Catalog/Pages 1 0 R>>\r\nendobj\r\n", pPDF->pdfObj);
		pPDF->currentOffSet += nChars;	pTail += nChars;

		/* Pages Object. It's always the Object 1 */
		Jpeg2PDF_SetXREF(pPDF, 1, pPDF->currentOffSet, 'n');

		strKids[0] = 0;
		pNode = pPDF->pFirstNode;
		while(pNode != NULL) {
			UINT8 curStr[9];
			sprintf(curStr, "%d 0 R ", pNode->PageObj);
			strcat(strKids, curStr);
			pNode = pNode->pNext;
		}

		if(strlen(strKids) > 1 && strKids[strlen(strKids) - 1] == ' ') strKids[strlen(strKids) - 1] = 0;

		nChars = sprintf(pTail, "1 0 obj\r\n<</Type /Pages /Kids [%s] /Count %d>>\r\nendobj\r\n", strKids, pPDF->nodeCount);
		pPDF->currentOffSet += nChars;	pTail += nChars;

		/* The xref & the rest of the tail */
		xrefOffSet = pPDF->currentOffSet;
		nChars = sprintf(pTail, "xref\r\n0 %d\r\n", pPDF->pdfObj+1);
		pPDF->currentOffSet += nChars;	pTail += nChars;

		for(i=0; i<=pPDF->pdfObj; i++) {
			 nChars = sprintf(pTail, "%s", pPDF->pdfXREF[i]);
			 pPDF->currentOffSet += nChars;	pTail += nChars;
		}

		nChars = sprintf(pTail, "trailer\r\n<</Root %d 0 R /Size %d>>\r\n", pPDF->pdfObj, pPDF->pdfObj+1);
		pPDF->currentOffSet += nChars;	pTail += nChars;

		nChars = sprintf(pTail, "startxref\r\n%d\r\n%%%%EOF\r\n", xrefOffSet);
		pPDF->currentOffSet += nChars;	pTail += nChars;
	}

	headerSize = strlen(pPDF->pdfHeader);
	tailerSize = strlen(pPDF->pdfTailer);
	if( headerSize && tailerSize && ( pPDF->currentOffSet > headerSize + tailerSize ) ) {
		pdfSize = pPDF->currentOffSet;
	}

	return pdfSize;
}

STATUS Jpeg2PDF_GetFinalDocumentAndCleanup(PJPEG2PDF pPDF, UINT8 *outPDF, UINT32 *outPDFSize) {
	STATUS result = ERROR;

	if(pPDF) {
		PJPEG2PDF_NODE pNode, pFreeCurrent;

		if(outPDF && (*outPDFSize >= pPDF->currentOffSet)) {
			UINT32 nBytes, nBytesOut = 0;
			UINT8 *pOut = outPDF;

			nBytes = strlen(pPDF->pdfHeader);
			memcpy(pOut, pPDF->pdfHeader, nBytes);
			nBytesOut += nBytes; pOut += nBytes;

			pNode = pPDF->pFirstNode;
			while(pNode != NULL) {
				nBytes = strlen(pNode->preFormat);
				memcpy(pOut, pNode->preFormat, nBytes);
				nBytesOut += nBytes; pOut += nBytes;

				nBytes = pNode->JpegSize;
				memcpy(pOut, pNode->pJpeg, nBytes);
				nBytesOut += nBytes; pOut += nBytes;

				nBytes = strlen(pNode->pstFormat);
				memcpy(pOut, pNode->pstFormat, nBytes);
				nBytesOut += nBytes; pOut += nBytes;

				pNode = pNode->pNext;
			}

			nBytes = strlen(pPDF->pdfTailer);
			memcpy(pOut, pPDF->pdfTailer, nBytes);
			nBytesOut += nBytes; pOut += nBytes;

			*outPDFSize = nBytesOut;
			result = OK;
			__android_log_write(ANDROID_LOG_ERROR,"Tag","result = OK;");
		}

		pNode = pPDF->pFirstNode;
		while(pNode != NULL) {

		__android_log_write(ANDROID_LOG_ERROR,"Tag","pNode != NULL");
			if(pNode->pJpeg) free(pNode->pJpeg);
			pFreeCurrent = pNode;
			pNode = pNode->pNext;
			free(pFreeCurrent);
		}

		if(pPDF) {
			free(pPDF);
			pPDF = NULL;
			__android_log_write(ANDROID_LOG_ERROR,"Tag","pPDF = NULL;");
		}
	}

	__android_log_write(ANDROID_LOG_ERROR,"Tag","pPDF = NULL;");

	return result;
}



//Gets the JPEG size from the array of data passed to the function, file reference: http://www.obrador.com/essentialjpeg/headerinfo.htm
static int get_jpeg_size(unsigned char* data, unsigned int data_size, unsigned short *width, unsigned short *height) {
	//Check for valid JPEG image
	int i=0;   // Keeps track of the position within the file
	if(data[i] == 0xFF && data[i+1] == 0xD8 && data[i+2] == 0xFF && data[i+3] == 0xE0) {
		i += 4;
		// Check for valid JPEG header (null terminated JFIF)
		if(data[i+2] == 'J' && data[i+3] == 'F' && data[i+4] == 'I' && data[i+5] == 'F' && data[i+6] == 0x00) {
			//Retrieve the block length of the first block since the first block will not contain the size of file
			unsigned short block_length = data[i] * 256 + data[i+1];
			while(i<(int)data_size) {
				i+=block_length;               //Increase the file index to get to the next block
				if(i >= (int)data_size) return 0;   //Check to protect against segmentation faults
				if(data[i] != 0xFF) return 0;   //Check that we are truly at the start of another block
				if(data[i+1] == 0xC0) {            //0xFFC0 is the "Start of frame" marker which contains the file size
					//The structure of the 0xFFC0 block is quite simple [0xFFC0][ushort length][uchar precision][ushort x][ushort y]
					*height = data[i+5]*256 + data[i+6];
					*width = data[i+7]*256 + data[i+8];
					return 1;
				}
				else
				{
					i+=2;                              //Skip the block marker
					block_length = data[i] * 256 + data[i+1];   //Go to the next block
				}
			}
			__android_log_write(ANDROID_LOG_ERROR,"Tag","If this point is reached then no size was found");
			return 0;                     //If this point is reached then no size was found
		}else{
		__android_log_write(ANDROID_LOG_ERROR,"Tag","Not a valid JFIF string");
		return 0; }                  //Not a valid JFIF string

	}else{
	__android_log_write(ANDROID_LOG_ERROR,"Tag","Not a valid SOI header");
	 return 0; }                     //Not a valid SOI header
}



void insertJPEGFile(const char *fileName, PJPEG2PDF pdfId) {
	FILE  *fp;
	unsigned char *jpegBuf;
	int readInSize;
	unsigned short jpegImgW, jpegImgH;
	int fileSize;


    __android_log_write(ANDROID_LOG_ERROR,"Tag6","begin open file");


      struct stat buf;
      int fd;
      fd = open (fileName,O_RDONLY);
      fstat(fd,&buf);


      fileSize =  buf.st_size;


	jpegBuf =  malloc(fileSize);

       //close(fp);
	   //if(fp = fopen(fileName, "rb") == NULL)
	   //{
	   //};

       __android_log_write(ANDROID_LOG_ERROR,"Tag","begin read image");

       readInSize = read(fd, jpegBuf, fileSize);


	   //readInSize = fread(jpegBuf, sizeof(char), fileSize, fp);
	 __android_log_write(ANDROID_LOG_ERROR,"Tag7","read end");
		//fclose(fp);
      close(fd);
	if(readInSize != fileSize)
		{
		 __android_log_write(ANDROID_LOG_ERROR,"Tag7","readInSize != fileSize");
}

	if(1 == get_jpeg_size(jpegBuf, readInSize, &jpegImgW, &jpegImgH)) {

		  //Add JPEG File into PDF
		Jpeg2PDF_AddJpeg(pdfId, jpegImgW, jpegImgH, readInSize, jpegBuf, 1);
	} else {
		 __android_log_write(ANDROID_LOG_ERROR,"Tag","formt eror");
	}

	free(jpegBuf);
}

JNIEXPORT void JNICALL Java_com_asus_supernote_pdfconverter_PdfConverter_Jpg2Pdf
  (JNIEnv * env, jobject obj, jobjectArray jpgPathArray, jstring pdfPath)
  {
	int iter;
	jsize pathCount;
	jstring jpgPath;
	const char *utf8JpgPath;
	const char *utf8PdfPath;
	PJPEG2PDF pdfId;

	pathCount = (*env)->GetArrayLength(env, jpgPathArray);
	if (pathCount <= 0) {
		return;
	}

	utf8PdfPath = (*env)->GetStringUTFChars(env, pdfPath, NULL);
	if (utf8PdfPath == NULL) {
		return;
	}

  		/* Initilized the PDF Object with Page Size Information */
	pdfId = Jpeg2PDF_BeginDocument(8.5, 11.0);	/* Letter is 8.5x11 inch */

	if(pdfId >= 0) {
		UINT32 pdfSize, pdfFinalSize;
		UINT8  *pdfBuf;

		for(iter = 0; iter < pathCount; iter++) {
			jpgPath = (*env)->GetObjectArrayElement(env, jpgPathArray, iter);
			utf8JpgPath = (*env)->GetStringUTFChars(env, jpgPath, NULL);
			if (utf8JpgPath == NULL) {
				continue;
			} else {
				/* Process the first found jpeg file */
				insertJPEGFile(utf8JpgPath, pdfId);
			}
			(*env)->ReleaseStringUTFChars(env, jpgPath, utf8JpgPath);
		}

        __android_log_write(ANDROID_LOG_ERROR,"Tag2","insert jpegfile success");

		/* Finalize the PDF and get the PDF Size */
		pdfSize = Jpeg2PDF_EndDocument(pdfId);
		/* Prepare the PDF Data Buffer based on the PDF Size */
		pdfBuf = malloc(pdfSize);

		__android_log_write(ANDROID_LOG_ERROR,"Tag3","Generating PDF File...");
		
		pdfFinalSize = pdfSize;

		/* Get the PDF into the Data Buffer and do the cleanup */
     	 Jpeg2PDF_GetFinalDocumentAndCleanup(pdfId, pdfBuf, &pdfFinalSize);
		__android_log_write(ANDROID_LOG_ERROR,"Tag4","Generating PDF Done");


		/* Output the PDF Data Buffer to file */
		{
			//FILE *fp = fopen("/sdcard/DCIM/Camera/output.pdf", "wb");
			int fd;
            fd = open (utf8PdfPath, O_WRONLY|O_CREAT£¬ 0777);
			//fwrite(pdfBuf, sizeof(UINT8), pdfFinalSize, fp);


			write(fd, pdfBuf, pdfFinalSize);
			close(fd);

		    __android_log_write(ANDROID_LOG_ERROR,"Tag5","putout pdf success");
		}

		free(pdfBuf);
		 __android_log_write(ANDROID_LOG_ERROR,"Tag5","free success!");

	} else {
		__android_log_write(ANDROID_LOG_ERROR,"Tag5","Error Init");
	}

	(*env)->ReleaseStringUTFChars(env, pdfPath, utf8PdfPath);
  }
