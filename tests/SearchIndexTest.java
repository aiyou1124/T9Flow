import com.qingdial.app.SearchIndex;
import com.ibm.icu.text.Transliterator;

/** Run with ICU4J on a desktop JVM; uses the same ICU transformation as Android. */
public class SearchIndexTest {
    static int checks;
    static void check(boolean value, String message) { checks++; if(!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        Transliterator tr=Transliterator.getInstance("Han-Latin; Latin-ASCII; Lower");
        SearchIndex z=new SearchIndex("张三","+86 138-0013-8000",tr::transliterate);
        check(z.initials.equals("zs"),"Chinese initials");
        for(String q:new String[]{"张三","张","zs","ZS","zhangsan","zhangs","97","1380013","8000"})check(z.score(q)>0,"Expected match: "+q);
        for(String q:new String[]{"lisi","李四","999999","😀","***"})check(z.score(q)==0,"Expected no match: "+q);
        check(z.score("zs")>z.score("s"),"Exact initials rank higher");
        check(new SearchIndex("单田芳","123",tr::transliterate).score("stf")>0,"Surname shan");
        check(new SearchIndex("曾小贤","123",tr::transliterate).score("zxx")>0,"Surname zeng");
        check(new SearchIndex("张小三","123",tr::transliterate).score("zs")>0,"Fuzzy subsequence");
        check(new SearchIndex("Alice 李","123",tr::transliterate).score("alice")>0,"Mixed Latin Chinese");
        check(new SearchIndex("欧阳娜娜","123",tr::transliterate).score("oynn")>0,"Compound surname");
        check(new SearchIndex("",null,tr::transliterate).score("abc")==0,"Empty data");
        check(SearchIndex.t9("abcdefghijklmnopqrstuvwxyz").equals("22233344455566677778889999"),"T9 mapping");
        SearchIndex wife=new SearchIndex("媳妇儿","13900000001",tr::transliterate);
        check(wife.score("933")>0,"933 matches each name character x-f-e");
        check(wife.score("93")>0,"Name prefix matches");
        check(wife.score("339")==0,"Reversed name order must not match");
        check(new SearchIndex("测试联系人","15900000000",tr::transliterate).score("933")==0,"Pinyin interiors must not match name initials");
        check(new SearchIndex("李四","13893300000",tr::transliterate).score("933")==0,"Short query must not match incidental phone substring");
        SearchIndex fuzzy=new SearchIndex("媳小妇儿","123",tr::transliterate);
        check(fuzzy.score("933")>0,"Ordered skipped characters match T9");
        check(fuzzy.score("媳儿")>0,"Ordered Chinese fuzzy match");
        check(fuzzy.score("儿媳")==0,"Reversed Chinese must not match");
        check(z.score("79")==0,"Reverse T9 initials rejected");
        check(SearchIndex.firstMatch(java.util.List.of(fuzzy,wife),"933")==wife,"Dial target is highest ranked match");
        SearchIndex same=new SearchIndex("媳妇儿","522",tr::transliterate);
        check(SearchIndex.firstMatch(java.util.List.of(wife,same),"933")==wife,"Equal scores keep first displayed contact");
        check(SearchIndex.firstMatch(java.util.List.of(wife),"")==null,"Empty query cannot dial a contact");
        check(java.util.Arrays.equals(wife.matchedNamePositions("933"),new int[]{0,1,2}),"Highlight all three mapped characters");
        check(java.util.Arrays.equals(new SearchIndex("媳李妇儿","123",tr::transliterate).matchedNamePositions("933"),new int[]{0,2,3}),"Highlight excludes skipped character");
        check(java.util.Arrays.equals(fuzzy.matchedNamePositions("933"),new int[]{1,2,3}),"Contiguous matching uses the same best path as search ranking");
        check(java.util.Arrays.equals(wife.matchedNamePositions("33"),new int[]{1,2}),"Highlight substring positions");
        check(wife.matchedNamePositions("339").length==0,"Reverse match highlights nothing");
        check(java.util.Arrays.equals(fuzzy.matchedNamePositions("媳儿"),new int[]{0,3}),"Highlight ordered Chinese query");
        check(java.util.Arrays.equals(z.matchedNamePositions("zhang"),new int[]{0}),"Full pinyin highlights its source character");
        check(java.util.Arrays.equals(new SearchIndex("😀张三","123",tr::transliterate).matchedNamePositions("97"),new int[]{2,3}),"UTF16 mapping after emoji");
        check(z.matchedNamePositions("8000").length==0,"Number-only match does not highlight name");
        check(z.matchedNamePositions("").length==0,"Empty query highlights nothing");
        check(z.score("1")>0&&z.score("13")>0&&z.score("138")>0,"Leading 1 enables incremental number prefix match");
        check(z.score("139")==0,"Phone mode excludes nonmatching prefixes");
        check(new SearchIndex("李四","0086 13912345678",tr::transliterate).score("139")>0,"Phone prefix handles 0086 country code");
        check(new SearchIndex("13号联系人","01088888888",tr::transliterate).score("13")==0,"Phone mode must match phone, not digits in name");
        check(z.matchedNamePositions("1").length==0,"Phone prefix does not highlight unrelated name characters");
        check(z.score("*1#")==0,"Service code cannot select a contact");
        try{
            java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
            try(java.io.ObjectOutputStream out=new java.io.ObjectOutputStream(bytes)){out.writeObject(fuzzy);}
            SearchIndex restored;
            try(java.io.ObjectInputStream in=new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray()))){restored=(SearchIndex)in.readObject();}
            check(restored.score("933")==fuzzy.score("933")&&java.util.Arrays.equals(restored.matchedNamePositions("933"),fuzzy.matchedNamePositions("933")),"Cached index retains search ranking and character highlights");
        }catch(Exception e){throw new AssertionError("Cache round trip failed",e);}
        System.out.println("PASS: "+checks+" search checks; 张三 → "+z.initials+" → "+z.initialsT9);
    }
}
