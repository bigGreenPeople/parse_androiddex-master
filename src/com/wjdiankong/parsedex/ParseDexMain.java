package com.wjdiankong.parsedex;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import com.wjdiankong.parsedex.struct.CodeItem;

public class ParseDexMain {

	private static Map<String, CodeItem> codeItemMap = new HashMap<String, CodeItem>();

	public static void main(String[] args) {

		byte[] srcByte = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;

		try {
			fis = new FileInputStream("dex/CoreDex.dex");
			bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			srcByte = bos.toByteArray();
		} catch (Exception e) {
			System.out.println("read res file error:" + e.toString());
		} finally {
			try {
				fis.close();
				bos.close();
			} catch (Exception e) {
				System.out.println("close file error:" + e.toString());
			}
		}

		if (srcByte == null) {
			System.out.println("get src error...");
			return;
		}

		System.out.println("ParseHeader:");
		ParseDexUtils.praseDexHeader(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		System.out.println("Parse StringIds:");
		ParseDexUtils.parseStringIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		System.out.println("Parse StringList:");
		ParseDexUtils.parseStringList(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		System.out.println("Parse TypeIds:");
		ParseDexUtils.parseTypeIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		System.out.println("Parse ProtoIds:");
		ParseDexUtils.parseProtoIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		System.out.println("Parse FieldIds:");
		ParseDexUtils.parseFieldIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		System.out.println("Parse MethodIds:");
		ParseDexUtils.parseMethodIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		System.out.println("Parse ClassIds:");
		ParseDexUtils.parseClassIds(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		System.out.println("Parse MapList:");
		ParseDexUtils.parseMapItemList(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		System.out.println("Parse Class Data:");
		ParseDexUtils.parseClassData(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		System.out.println("Parse Code Content:");
		ParseDexUtils.parseCode(srcByte);
		System.out.println("++++++++++++++++++++++++++++++++++++++++");

		codeItemMap.putAll(ParseDexUtils.directMethodCodeItemMap);

		// --------------------------------------------------------------------
		// 方法抽取逻辑
		String className = "Lcom/shark/calculate/CoreUtils;";
		String methodName = "getPwd#Ljava/lang/String;()L";
		String signName = className + methodName;
		// 遍历所有方法的信息
		for (String key : codeItemMap.keySet()) {
			System.out.println("key:" + key);
			// 找到想抽取的方法
			if (key.equals(signName)) {
				CodeItem codeItem = codeItemMap.get(key);
				// 获取方法对应的指令个数和偏移
				int insns_size = codeItem.insns_size;
				int insns_Offset = codeItem.insnsOffset;
				
				// 构造空指令 每条指令占两个字节
				byte[] nopBytes = new byte[insns_size * 2];
				for (int i = 0; i < nopBytes.length; i++) {
					nopBytes[i] = 0;
				}

				try {
					// 替换原有指令
					srcByte = Utils.replaceBytes(srcByte, nopBytes,
							insns_Offset);
					// 修改DEX file size文件头
					Utils.updateFileSizeHeader(srcByte);// dex中32到35的位置为文件长度
					// 修改DEX SHA1 文件头
					Utils.updateSHA1Header(srcByte);
					// dex中12到31位置，32到结束参与SHA1计算
					// 修改DEX CheckSum文件头
					Utils.updateCheckSumHeader(srcByte);// dex中8到11位置，12到文件结束计算checksum

					String str = "dex/new_CoreDex.dex";
					File file = new File(str);
					if (!file.exists()) {
						file.createNewFile();
					}
					FileOutputStream fileOutputStream = new FileOutputStream(
							file);
					fileOutputStream.write(srcByte);
					fileOutputStream.flush();
					fileOutputStream.close();
					System.out.println("done!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

}
