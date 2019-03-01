package com.cmri.tvdemo.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Tools {

	public static String getTextFromStream(InputStream is){
		byte[] b = new byte[1024];
		int len;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			while((len = is.read(b)) != -1){
				bos.write(b, 0, len);
			}
			//���ֽ����������ת�����ֽ����飬Ȼ�����ֽ����鹹��һ���ַ���
			String text = new String(bos.toByteArray());
			return text;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
}
