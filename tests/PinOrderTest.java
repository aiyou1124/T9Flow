import com.qingdial.app.*;
import java.util.*;
public class PinOrderTest {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static SearchIndex c(String n,String p){return new SearchIndex(n,p,s->s);}
 public static void main(String[] args){
  SearchIndex a=c("Alice","13800000001"),b=c("Albert","13800000002"),z=c("Zoe","13900000003");
  List<SearchIndex> all=Arrays.asList(a,b,z);PinOrder order=new PinOrder(Collections.emptyList());
  check(order.matches(all,"").equals(all),"Unpinned stable order");
  order.pin(z.name,z.number);order.pin(b.name,b.number);order.pin(z.name,z.number);
  check(order.saved().size()==2,"Duplicate pin ignored");
  check(order.matches(all,"").equals(Arrays.asList(z,b,a)),"Pin order precedes ordinary contacts");
  check(order.matches(all,"al").equals(Arrays.asList(b,a)),"Nonmatching pins excluded");
  check(order.firstMatch(all,"al")==b,"Call target equals visible first result");
  check(order.firstMatch(all,"0000000000000000")==null,"No match does not call a pin");
  order.move(b.name,b.number,0);check(order.matches(all,"").equals(Arrays.asList(b,z,a)),"Move upward");
  order.move(b.name,b.number,1);check(order.matches(all,"").equals(Arrays.asList(z,b,a)),"Move downward");
  PinOrder restored=new PinOrder(order.saved());check(restored.matches(all,"").equals(order.matches(all,"")),"Serialized order survives restore");
  check(restored.position("Zoe","+86 139-0000-0003")==0,"Phone formatting normalized");
  check(restored.position("Other","13900000003")==-1,"Same number different names distinguished");
  check(restored.position("Zoe","13900000004")==-1,"Multiple numbers distinguished");
  restored.unpin(z.name,z.number);check(restored.matches(all,"").equals(Arrays.asList(b,a,z)),"Unpin restores ordinary order");
  restored.move(b.name,b.number,-100);check(restored.position(b.name,b.number)==0,"Move clamps to start");
  restored.move(b.name,b.number,100);check(restored.position(b.name,b.number)==0,"Move clamps to end");
  restored.pin("Hidden",null);check(restored.saved().size()==1,"Hidden number cannot be pinned");
  check(new PinOrder(Arrays.asList("x","x","",null)).saved().equals(Arrays.asList("x")),"Invalid saved entries ignored");
  check(order.firstMatch(all,"")==null,"Empty query does not dial automatically");
  check(order.matches(Arrays.asList(a),"").equals(Arrays.asList(a)),"Deleted contacts not displayed");
  System.out.println("PinOrder: "+checks+" checks passed");
 }
}
