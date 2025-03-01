package by.alexhome.t1schoolproject.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspectAfterReturning {

    public static final Logger logger = LoggerFactory.getLogger(LoggingAspectAfterReturning.class);

    @AfterReturning("@annotation(by.alexhome.t1schoolproject.aspect.annotation.LoggingAfter))")
    public void logAfterReturning(JoinPoint joinPoint) {
        logger.info("After execution method: {}", joinPoint.getSignature().getName());
    }
}