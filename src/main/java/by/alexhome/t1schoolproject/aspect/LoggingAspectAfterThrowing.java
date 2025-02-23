package by.alexhome.t1schoolproject.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspectAfterThrowing {

    public static final Logger logger = LoggerFactory.getLogger(LoggingAspectAfterThrowing.class);

    @AfterThrowing(pointcut = "@annotation(by.alexhome.t1schoolproject.aspect.annotation.LoggingThrowing)", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        logger.info("Exception in method: {} exception = {}", joinPoint.getSignature().getName(), ex.getMessage());
    }
}