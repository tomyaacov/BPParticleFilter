package il.ac.bgu.cs.bp.samplebpjsproject;

public class ThreadTest {

    public static void main(final String[] args){
        for (int i = 0; i < 4000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                    }
                }
            }).start();
            System.out.println(i);
        }
    }
}
