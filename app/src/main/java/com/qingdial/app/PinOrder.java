package com.qingdial.app;

import java.util.*;

/** Ordered local favorites, applied only after the usual matching filter. */
public final class PinOrder {
    private final ArrayList<String> keys=new ArrayList<>();
    public PinOrder(Collection<String> saved){for(String key:saved)if(key!=null&&!key.isEmpty()&&!keys.contains(key))keys.add(key);}
    public static String key(String name,String number){
        String n=name==null?"":name,d=SearchIndex.digits(number==null?"":number);
        if(d.length()==13&&d.startsWith("861"))d=d.substring(2);
        else if(d.length()==15&&d.startsWith("00861"))d=d.substring(4);
        return d.isEmpty()?"":n.length()+":"+n+":"+d;
    }
    public int position(String name,String number){return keys.indexOf(key(name,number));}
    public List<String> saved(){return new ArrayList<>(keys);}
    public void pin(String name,String number){String k=key(name,number);if(!k.isEmpty()&&!keys.contains(k))keys.add(k);}
    public void unpin(String name,String number){keys.remove(key(name,number));}
    public void move(String name,String number,int target){int at=position(name,number);if(at<0)return;String k=keys.remove(at);keys.add(Math.max(0,Math.min(target,keys.size())),k);}
    public List<SearchIndex> matches(List<SearchIndex> contacts,String text){
        SearchIndex.Query query=new SearchIndex.Query(text);
        ArrayList<SearchIndex> found=new ArrayList<>();Map<SearchIndex,Integer> scores=new IdentityHashMap<>();
        for(SearchIndex c:contacts){int score=c.score(query);if(score>0){found.add(c);scores.put(c,score);}}
        found.sort((a,b)->{int pa=position(a.name,a.number),pb=position(b.name,b.number);
            if(pa>=0||pb>=0){int order=Integer.compare(pa<0?Integer.MAX_VALUE:pa,pb<0?Integer.MAX_VALUE:pb);if(order!=0)return order;}
            return Integer.compare(scores.get(b),scores.get(a));});
        return found;
    }
    public SearchIndex firstMatch(List<SearchIndex> contacts,String text){if(text.trim().isEmpty())return null;List<SearchIndex> found=matches(contacts,text);return found.isEmpty()?null:found.get(0);}
}
