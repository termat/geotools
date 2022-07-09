package net.termat.geo.citygml;

import java.util.HashMap;
import java.util.Map;

public class BldgConstants {
	private static Map<String,String> classList=getClassCodeList();
	private static Map<String,String> usageList=getUsageCodeList();
	private static Map<String,String> roofList=getRoofCodeList();
	
	private BldgConstants() {}
	
	private static Map<String,String> getClassCodeList(){
		Map<String,String> ret=new HashMap<>();
		ret.put("普通建物", "3001");
		ret.put("堅ろう建物","3002");
		ret.put("普通無壁舎", "3003");
		ret.put("堅ろう無壁舎", "3004");
		ret.put("分類しない建物", "3000");
		return ret;
	}
	
	private static Map<String,String> getUsageCodeList(){
		Map<String,String> ret=new HashMap<>();
		ret.put("業務施設", "401");
		ret.put("商業施設", "402");
		ret.put("宿泊施設", "403");
		ret.put("商業系複合施設", "404");
		ret.put("住宅", "411");
		ret.put("共同住宅", "412");
		ret.put("店舗等併用住宅", "413");
		ret.put("店舗等併用共同住宅", "414");
		ret.put("作業所併用住宅", "415");
		ret.put("官公庁施設", "421");
		ret.put("文教厚生施設", "422");
		ret.put("運輸倉庫施設", "431");
		ret.put("工場", "441");
		ret.put("農林漁業用施設", "451");
		ret.put("供給処理施設", "452");
		ret.put("防衛施設", "453");
		ret.put("その他", "454");
		ret.put("不明", "461");
		return ret;
	}
	
	public static Map<String,String> getRoofCodeList(){
		Map<String,String> ret=new HashMap<>();
		ret.put("切妻屋根", "1");
		ret.put("寄棟屋根","2");
		ret.put("方形屋根", "3");
		ret.put("陸屋根", "4");
		ret.put("片流れ屋根", "5");
		ret.put("袴腰屋根/半切妻屋根", "6");
		ret.put("入母屋屋根", "7");
		ret.put("錣（しころ）屋根", "8");
		ret.put("マンサード屋根", "9");
		ret.put("越屋根", "10");
		ret.put("招き屋根", "11");
		ret.put("差し掛け屋根 ", "12");
		ret.put("バタフライ屋根 ", "13");
		ret.put("鋸屋根", "14");
		ret.put("六柱屋根", "25");
		ret.put("八柱屋根", "16");
		ret.put("M型屋根", "17");
		ret.put("下屋付招き屋根", "18");
		ret.put("棟違い屋根", "19");
		ret.put("乗り越し屋根", "20");
		ret.put("腰折れ屋根", "21");
		ret.put("隅切屋根", "22");
		ret.put("アーチ屋根", "23");
		ret.put("ドーム屋根", "24");
		ret.put("シェル屋根", "25");
		ret.put("カテナリー屋根", "26");
		ret.put("膜構造", "27");
		ret.put("その他", "28");
		ret.put("不明", "9020");
		return ret;
	}
	
	public static String getClassVal(String type) {
		return classList.get(type);
	}
	
	public static String getUsageVal(String type) {
		return usageList.get(type);
	}
	
	public static String getRoofVal(String type) {
		return roofList.get(type);
	}
}
