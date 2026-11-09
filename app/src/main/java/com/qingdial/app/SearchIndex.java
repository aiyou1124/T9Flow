package com.qingdial.app;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;

/** Immutable, cached search keys. The transliterator is supplied by Android ICU. */
public final class SearchIndex implements java.io.Serializable {
    private static final long serialVersionUID=1L;
    public final String name, number, full, initials, fullT9, initialsT9;
    private final String numberDigits;
    private final int[] initialPositions, fullPositions;
    public static final class Query {
        final String raw, normalized;
        final boolean numeric;
        public Query(String query){raw=query.trim();normalized=clean(raw);numeric=!normalized.isEmpty()&&normalized.chars().allMatch(c->c>='0'&&c<='9');}
    }
    public SearchIndex(String name, String number, Function<String,String> romanize) {
        this.name = name == null ? "" : name;
        this.number = number == null ? "" : number;
        numberDigits=digits(this.number);
        StringBuilder p = new StringBuilder(), i = new StringBuilder();
        ArrayList<Integer> ip=new ArrayList<>(),fp=new ArrayList<>();
        for (int offset = 0; offset < this.name.length();) {
            int cp = this.name.codePointAt(offset);
            String ch = new String(Character.toChars(cp));
            String syllable = clean(romanize.apply(ch));
            if (offset == 0) {
                String[] surnames = {"单:shan","解:xie","仇:qiu","区:ou","查:zha","曾:zeng","乐:yue","朴:piao","翟:zhai","尉:yu","缪:miao"};
                for (String pair : surnames) if (pair.startsWith(ch + ":")) syllable = pair.substring(2);
            }
            p.append(syllable);
            for(int j=0;j<syllable.length();j++)fp.add(offset);
            if (!syllable.isEmpty()){i.append(syllable.charAt(0));ip.add(offset);}
            offset += Character.charCount(cp);
        }
        full = p.toString(); initials = i.toString(); fullT9 = t9(full); initialsT9 = t9(initials);
        initialPositions=ip.stream().mapToInt(Integer::intValue).toArray();fullPositions=fp.stream().mapToInt(Integer::intValue).toArray();
    }
    public static String clean(String s) {
        return Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").replaceAll("[^a-z0-9]", "");
    }
    public static String digits(String s) { return s.replaceAll("[^0-9]", ""); }
    public static String t9(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : clean(s).toCharArray()) {
            if (c >= 'a' && c <= 'z') out.append("22233344455566677778889999".charAt(c-'a'));
            else out.append(c);
        }
        return out.toString();
    }
    public int score(String query) {
        return score(new Query(query));
    }
    public int score(Query query) {
        if (query.raw.isEmpty()) return 1;
        if(query.raw.indexOf('*')>=0||query.raw.indexOf('#')>=0)return 0;
        if(query.numeric&&query.normalized.startsWith("1")){
            String phone=numberDigits;
            if(phone.length()==13&&phone.startsWith("861"))phone=phone.substring(2);
            else if(phone.length()==15&&phone.startsWith("00861"))phone=phone.substring(4);
            return phone.startsWith(query.normalized)?(phone.equals(query.normalized)?100:90):0;
        }
        if (name.contains(query.raw)) return 100;
        if(query.raw.codePoints().anyMatch(c->Character.UnicodeScript.of(c)==Character.UnicodeScript.HAN)
            && ordered(name,query.raw))return 75;
        String q = query.normalized;
        if (q.isEmpty()) return 0;
        if (q.equals(initials) || q.equals(full)) return 95;
        if (query.numeric) {
            if (numberDigits.equals(q)) return 100;
            if (initialsT9.equals(q)) return 92;
            if (initialsT9.startsWith(q)) return 85;
            if (initialsT9.contains(q)) return 75;
            if (ordered(initialsT9,q)) return 60;
            // Short T9 queries represent name characters, not incidental phone digits.
            if (q.length()>=4 && numberDigits.contains(q)) return 50;
        } else {
            if (initials.startsWith(q) || full.startsWith(q)) return 85;
            if (initials.contains(q) || full.contains(q)) return 70;
            if (ordered(initials,q)) return 40;
        }
        return 0;
    }
    private static boolean ordered(String source,String query){
        int[] wanted=query.codePoints().toArray();int n=0;
        for(int cp:source.codePoints().toArray())if(n<wanted.length&&cp==wanted[n])n++;
        return n==wanted.length;
    }
    /** UTF-16 starts of exactly the name characters consumed by this query. */
    public int[] matchedNamePositions(String text){
        Query query=new Query(text);
        if(query.raw.isEmpty())return new int[0];
        if(query.raw.indexOf('*')>=0||query.raw.indexOf('#')>=0||(query.numeric&&query.normalized.startsWith("1")))return new int[0];
        int direct=name.indexOf(query.raw);
        if(direct>=0){ArrayList<Integer> out=new ArrayList<>();for(int at=direct;at<direct+query.raw.length();at+=Character.charCount(name.codePointAt(at)))out.add(at);return out.stream().mapToInt(Integer::intValue).toArray();}
        if(query.raw.codePoints().anyMatch(c->Character.UnicodeScript.of(c)==Character.UnicodeScript.HAN)){
            int[] wanted=query.raw.codePoints().toArray();ArrayList<Integer> out=new ArrayList<>();int n=0;
            for(int at=0;at<name.length()&&n<wanted.length;){int cp=name.codePointAt(at);if(cp==wanted[n]){out.add(at);n++;}at+=Character.charCount(cp);}
            return n==wanted.length?out.stream().mapToInt(Integer::intValue).toArray():new int[0];
        }
        if(query.normalized.isEmpty())return new int[0];
        if(query.numeric)return mapMatch(initialsT9,query.normalized,initialPositions,true);
        int[] initial=mapMatch(initials,query.normalized,initialPositions,false);
        if(initial.length>0)return initial;
        int[] pinyin=mapMatch(full,query.normalized,fullPositions,false);
        return pinyin.length>0?pinyin:mapMatch(initials,query.normalized,initialPositions,true);
    }
    private static int[] mapMatch(String source,String query,int[] positions,boolean allowGaps){
        int contiguous=source.indexOf(query);LinkedHashSet<Integer> out=new LinkedHashSet<>();
        if(contiguous>=0){for(int j=0;j<query.length();j++)out.add(positions[contiguous+j]);}
        else if(allowGaps){int at=0;for(int j=0;j<source.length()&&at<query.length();j++)if(source.charAt(j)==query.charAt(at)){out.add(positions[j]);at++;}if(at!=query.length())out.clear();}
        return out.stream().mapToInt(Integer::intValue).toArray();
    }
    /** Same stable tie ordering as the displayed, score-sorted list. */
    public static SearchIndex firstMatch(List<SearchIndex> contacts,String text){
        if(text.trim().isEmpty())return null;
        Query query=new Query(text);SearchIndex first=null;int best=0;
        for(SearchIndex contact:contacts){int score=contact.score(query);if(score>best){best=score;first=contact;}}
        return first;
    }
}
