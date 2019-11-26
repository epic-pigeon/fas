import spos.lab1.demo.DoubleOps;

import java.lang.reflect.Field;
import java.sql.Time;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class Task {
    private Task() {}

    private Function<Double, Double> f, g;

    private long timeout;
    private TimeUnit timeoutUnit;
    private int stopKey = KeyUtils.ESCAPE;

    public void setStopKey(int stopKey) {
        this.stopKey = stopKey;
    }

    public Double run(Double val) {
        AbortableLatch latch = new AbortableLatch(2);
        AtomicReference<Double> resultF = new AtomicReference<>(), resultG = new AtomicReference<>();
        new Thread(() -> {
            Double result = f.apply(val);
            latch.countDown();
            if (result == null) throw new RuntimeException("Function F returned null!");
            if (result.equals(0D)) latch.countDown();
            resultF.set(result);
        }).start();
        new Thread(() -> {
            Double result = g.apply(val);
            latch.countDown();
            if (result == null) throw new RuntimeException("Function G returned null!");
            if (result.equals(0D)) latch.countDown();
            resultG.set(result);
        }).start();
        Thread waiting = null;
        if (timeoutUnit != null) {
            waiting = new Thread(() -> {
                try {
                    timeoutUnit.sleep(timeout);
                    latch.abort();
                } catch (InterruptedException ignored) {}
            });
            waiting.start();
        }
        if (stopKey > 0) {
            new Thread(() -> {
                while (true) {
                    if (KeyUtils.isKeyDown(stopKey)) {
                        latch.abort();
                    }
                }
            }).start();
        }
        try {
            latch.await();
            if (waiting != null) {
                waiting.interrupt();
            }
            return resultF.get() == null || resultG.get() == null ? 0D : resultG.get() * resultF.get();
        } catch (AbortableLatch.AbortedException e) {
            throw new RuntimeException("Calculation aborted", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //System.out.println(resultF);
        //System.out.println(resultG);
    }

    public static Task of(Function<Double, Double> f, Function<Double, Double> g) {
        Task result = new Task();
        result.f = f;
        result.g = g;
        return result;
    }

    public void setTimeout(long timeout, TimeUnit timeoutUnit) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }

    public static Task ofCase(Object theCase) {
        try {
            Class<?> caseClass = Class.forName("spos.lab1.demo.Case");
            assert theCase.getClass().equals(caseClass);
            Class<?> computationalAttrsClass = Class.forName("spos.lab1.demo.ComputationAttrs");
            Field fAttrsField = caseClass.getDeclaredField("fAttrs"); fAttrsField.setAccessible(true);
            Field gAttrsField = caseClass.getDeclaredField("gAttrs"); gAttrsField.setAccessible(true);
            Optional<?> fAttrsOptional = (Optional<?>) fAttrsField.get(theCase);
            Optional<?> gAttrsOptional = (Optional<?>) gAttrsField.get(theCase);
            Object fAttrs = fAttrsOptional.orElse(null);
            Object gAttrs = gAttrsOptional.orElse(null);
            Function<Double, Double> f, g;
            f = g = (val) -> {
                while (true);
            };
            Field attrsTimeUnit = computationalAttrsClass.getDeclaredField("timeUnit");
            attrsTimeUnit.setAccessible(true);
            Field attrsDelay = computationalAttrsClass.getDeclaredField("delay");
            attrsDelay.setAccessible(true);
            Field attrsResult = computationalAttrsClass.getDeclaredField("result");
            attrsResult.setAccessible(true);

            if (fAttrs != null) {
                //System.out.println("kar1");
                TimeUnit fAttrsTimeUnit = (TimeUnit) attrsTimeUnit.get(fAttrs);
                long fAttrsDelay = (long) attrsDelay.get(fAttrs);
                Double fAttrsResult = Double.valueOf(attrsResult.get(fAttrs).toString());
                f = (val) -> {
                    try {
                        fAttrsTimeUnit.sleep(fAttrsDelay);
                        return fAttrsResult;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                };
            }
            if (gAttrs != null) {
                //System.out.println("kar2");
                TimeUnit gAttrsTimeUnit = (TimeUnit) attrsTimeUnit.get(gAttrs);
                long gAttrsDelay = (long) attrsDelay.get(gAttrs);
                Double gAttrsResult = Double.valueOf(attrsResult.get(gAttrs).toString());
                g = (val) -> {
                    try {
                        gAttrsTimeUnit.sleep(gAttrsDelay);
                        return gAttrsResult;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                };
            }
            //System.out.println(fAttrsResult);
            //System.out.println(gAttrsResult);
            return of(f, g);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Task ofCaseIndex(int index, boolean isDouble) {
        try {
            Class<?> opsClass = isDouble ?
                    Class.forName("spos.lab1.demo.DoubleOps") :
                    Class.forName("spos.lab1.demo.IntOps");
            Field cases = opsClass.getDeclaredField("cases");
            cases.setAccessible(true);
            return ofCase(((Object[]) cases.get(null))[index]);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
