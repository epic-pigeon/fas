import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AbortableLatch extends CountDownLatch {
    private boolean aborted = false;

    public AbortableLatch(int count) {
        super(count);
    }


    public void abort() {
        if( getCount()==0 )
            return;

        this.aborted = true;
        while(getCount()>0)
            countDown();
    }


    public boolean isAborted() {
        return aborted;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        final boolean rtrn = super.await(timeout,unit);
        if (aborted)
            throw new AbortedException();
        return rtrn;
    }

    @Override
    public void await() throws InterruptedException {
        super.await();
        if (aborted)
            throw new AbortedException();
    }


    public static class AbortedException extends InterruptedException {
        public AbortedException() {
            super();
        }

        public AbortedException(String detailMessage) {
            super(detailMessage);
        }
    }
}