package by.alexhome.t1schoolproject.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspectAround {

    public static final Logger logger = LoggerFactory.getLogger(LoggingAspectAround.class);

    @Around("@annotation(by.alexhome.t1schoolproject.aspect.annotation.LoggingAround)")
    public Object logTimeExecution(ProceedingJoinPoint proceedingJoinPoint) {

        long startTimeMillis = System.currentTimeMillis();

        Object result = null;
        try {
            result = proceedingJoinPoint.proceed();
            logger.info("Method: {} execution : {} millisecond", proceedingJoinPoint.getSignature().getName(),
                    System.currentTimeMillis() - startTimeMillis);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result;
    }
}