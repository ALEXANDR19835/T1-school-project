package by.alexhome.t1schoolproject.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspectBefore {

    public static final Logger logger = LoggerFactory.getLogger(LoggingAspectBefore.class);

    @Before("@annotation(by.alexhome.t1schoolproject.aspect.annotation.LoggingBefore))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Before execution method: {}", joinPoint.getSignature().getName());
    }
}