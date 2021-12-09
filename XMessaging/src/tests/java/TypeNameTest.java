import org.junit.Test;

public class TypeNameTest {
    @Test
    public void testTypeName(){
        Object a = 1;
        System.out.println("Object a = 1 & a's type name : " + a.getClass().getTypeName());
        System.out.println("((Integer)1) type name : " + ((Integer)1).getClass().getTypeName());
        System.out.println("int type name : " + int.class.getTypeName());
    }
}