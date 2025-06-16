package com.asus.supernote.ui;

import android.graphics.Color;

public class ColorHelper {
	private static String[] mSampleColorCode=new String[]{  //Carol
			"#BC8F8F","#D87093","#E9967A","#A0522D","#FF69B4","#FFA07A","#8B4513","#F08080",
			"#CD853F","#F4A460","#CD5C5C","#FA8072","#A52A2A","#800000","#D2691E","#FF7550",
			"#B22222","#8B0000","#FF6347","#DC143C","#FF4500","#FF0000","#C71585","#FF1493",
			"#D2B48C","#808000","#B8860B","#DAA520","#FF8C00","#FFA500",
			"#8FBC8F","#2E8B57","#90EE90","#98FB98","#3CB371","#228B22","#008000","#32CD32",
			"#00FF7F","#7CFC00","#7FFF00","#00FF00","#ADD8E6","#B0E0E6","#AFEEEE","#66CDAA",
			"#7FFFD4","#48D1CC","#20B2AA","#40E0D0","#00FA9A","#00CED1","#00FFFF","#00FFFF",
			"#6B8E23","#9ACD32","#ADFF2F",
			"#006400","#B0C4DE","#778899","#708090","#483D8B","#9370D8","#191970","#6495ED",
			"#6A5ACD","#7B68EE","#4169E1","#000080","#00008B","#0000CD","#0000FF","#2F4F4F",
			"#5F9EA0","#87CEEB","#87CEFA","#4682B4","#008080","#008B8B","#1E90FF","#00BFFF",
			"#4B0082","#9932CC","#8A2BE2","#800080","#8B008B","#9400D3","#556B2F","#A9A9A9",
			"#808080","#696969","#000000",
			"#FFF0F5","#FFF5EE","#FAF0E6","#FFE4E1","#FFDAB9","#F5FFFA","#F0FFF0","#E6E6FA",
			"#F0F8FF","#F0FFFF","#E0FFFF","#FFFAF0","#FFFFF0","#FDF5E6","#F5F5DC","#FAEBD7",
			"#FFFFE0","#FFF8DC","#FFEFD5","#FAFAD2","#FFEBCD","#FFFACD","#FFE4C4","#F5DEB3",
			"#FFE4B5","#EEE8AA","#FFDEAD","#DEB887","#BDB76B","#F0E68C","#FFD700","#FFFF00",
			"#FFFFFF","#F5F5F5","#FFFAFA","#DCDCDC","#F8F8FF","#D3D3D3","#C0C0C0",
			"#FFC0CB","#FFB6C1","#D8BFD8","#DDA0DD","#EE82EE","#DA70D6","#BA55D3","#FF00FF",
			"#FF00FF",
			"#B4B4B4", "#5A5A5A","#E70012", "#FF9900","#FFF100", "#8FC31F", "#009944", "#00A0E9", 
			"#1D2088","#E5007F"
	};
	
	private static String[] mSampleColorName=new String[]{  //Carol
			"Rosy\nBrown","Pale\nViolet\nRed","Dark\nSalmon","Sienna","Hot\nPink",
			"Light\nSalmon","Saddle\nBrown","Light\nCoral","Peru","Sandy\nBrown",
			"Indian\nRed","Salmon","Brown","Maroon","Chocolate",
			"Coral","Fire\nBrick","Dark\nRed","Tomato","Crimson",
			"Orange\nRed","Red","Medium\nViolet\nRed","Deep\nPink","Tan",
			"Olive","Dark\nGolden\nRod","Golden\nRod","Dark\nOrange","Orange",
			"Dark\nSea\nGreen","Sea\nGreen","Light\nGreen","Pale\nGreen","Medium\nSea\nGreen",
			"Forest\nGreen","Green","Lime\nGreen","Spring\nGreen","Lawn\nGreen",
			"Chartreuse","Lime","Light\nBlue","Powder\nBlue","Pale\nTurquoise",
			"Medium\nAqua\nMarine","Aquamarine","Medium\nTurquolse","Light\nSea\nGreen","Turquolse",
			"Medium\nSpring\nGreen","Dark\nTurquolse","Aqua","Cyan","Olive\nDrab",
			"Yellow\nGreen","Green\nYellow",
			"Dark\nGreen","Light\nSteel\nBlue","Light\nSlate\nGray","Slate\nGray","Dark\nSlate\nBlue",
			"Medium\nPurple","Midnight\nBlue","Cornflower\nBlue","Slate\nBlue","Medium\nSlate\nBlue",
			"Royal\nBlue","Navy","Dark\nBlue","Medium\nBlue","Blue",
			"Dark\nSlate\nGray","Cadet\nBlue","Sky\nBlue","Light\nSky\nBlue","Steel\nBlue",
			"Teal","Dark\nCyan","Dodgerblue","Deep\nSky\nBlue","Indigo",
			"Dark\nOrchid","Blue\nViolet","Purple","Dark\nMagenta","Dark\nViolet",
			"Dark\nOlive\nGreen","Dark\nGray","Gray","Dim\nGray","Black",
			"Lanvender\nBlush","Sea\nShell","Linen","Misty\nRose","Peach\nPuff",
			"Mint\nCream","Honey\nDew","Lavender","Alice\nBlue","Azure",
			"Light\nCyan","Floral\nWhite","Ivory","Old\nLace","Beige",
			"Antique\nWhite","Light\nYellow","Cornsilk","Papaya\nWhip","Light\nGoldenRod\nYellow",
			"Blanched\nAlmond","Lemon\nChiffon","Bisque","Wheat","Moccasin",
			"Pale\nGolden\nRod","Navajo\nWhite","Burly\nWood","Dark\nKhaki","Khaki",
			"Gold","Yellow","White","White\nSmoke","Snow",
			"Gainsboro","Ghost\nWhite","Light\nGray","Silver",
			"Pink","Light\nPink","Thistle","Plum","Violet",
			"Orchid","Medium\nOrchid","Fuchsia","Magenta",
    		"Gray","Dark\nGray","Red","Orange","Yellow",
    		"Green","Lime\nGreen","Blue","Dark\nBlue","Pink"
	};
	
	public static String displayColorName(int value){ //Carol
		//name the chosen color
		double smallest = 200000;
		int mark = -1;
		int r1 = Color.red(value);
		int g1 = Color.green(value);
		int b1 = Color.blue(value);
		for(int i=0;i<mSampleColorCode.length;i++){
			int r2 = Color.red(Color.parseColor(mSampleColorCode[i]));
			int g2 = Color.green(Color.parseColor(mSampleColorCode[i]));
			int b2 = Color.blue(Color.parseColor(mSampleColorCode[i]));
			if(r1==r2&&g1==g2&&b1==b2)
				return mSampleColorName[i];
			else{
				//calculate the closeness of two color
				double relatedness = Math.abs(r1*r1-r2*r2)*0.34+Math.abs(g1*g1-g2*g2)*0.41+Math.abs(b1*b1-b2*b2)*0.25;
				if(relatedness < smallest){
					smallest = relatedness;
					mark=i;
				}
			}
		}
		return mSampleColorName[mark];
	}
}
