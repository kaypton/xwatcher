public class TestNetdataAPI {
    /*
    @Test
    public void testCpuUtil(){
        NetdataAPI.setHost("222.201.144.196:19999");
        NetdataAPI api = new NetdataAPI();

        while(true){
            NetdataCpuUtilMetric metric = api.getCpuUtil();
            System.out.println(metric.getLatestMetric(NetdataCpuUtilMetric.Kind.SYSTEM).getUtil()
             + " -> " + metric.getLatestMetric(NetdataCpuUtilMetric.Kind.SYSTEM).getTimestamp());
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testMemUtil(){
        NetdataAPI.setHost("222.201.144.196:19999");
        NetdataAPI api = new NetdataAPI();

        while(true){
            NetdataMemUtilMetric metric = api.getMemUtil();
            System.out.println(metric.getLatestMetric(NetdataMemUtilMetric.Kind.USED).getUtil()
                    + " -> " + metric.getLatestMetric(NetdataMemUtilMetric.Kind.USED).getTimestamp());
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }*/
}
