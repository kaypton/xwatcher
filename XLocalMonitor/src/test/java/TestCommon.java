import org.junit.Test;

public class TestCommon {
    @Test
    public void testSplit(){
        String a = "instance-11111";

        String[] aa = a.split("-");
        StringBuilder stringBuilder = new StringBuilder();
        for(String aaa : aa){
            stringBuilder.append(aaa);
        }
        System.out.println(stringBuilder.toString());
    }
}
