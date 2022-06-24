package org.youzipi.spring.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author wuqiantai
 * @date 2019-06-13 11:25
 */
@Slf4j
@Component
public class EnvUtil {
    @Resource
    private Environment environment;

    public boolean isTest() {
        return isEnv("test");
    }

    public boolean isNotTest() {
        return !isTest();
    }

    public boolean isDev() {
        return isEnv("dev");
    }

    public boolean isAlpha() {
        return isEnv("alpha");
    }

    public boolean isFree() {
        return isEnv("free");
    }

    public boolean isProd() {
        return isEnv("prod");
    }

    public String env() {
        return environment.getActiveProfiles()[0];
    }

    public boolean isNotProd() {
        return !isProd();
    }

    public boolean isEnv(String expectEnv) {
        String[] activeProfiles = curEnvArray();
        return Arrays.stream(activeProfiles).anyMatch(expectEnv::equalsIgnoreCase);
    }

    public String[] curEnvArray() {
        return environment.getActiveProfiles();
    }

}
