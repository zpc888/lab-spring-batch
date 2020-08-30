
package io.spring.batch.helloworld;

import org.springframework.stereotype.Component;

@Component
public class AccountService {
    public void transfer() {
        System.out.println(Thread.currentThread().getName() + " ... ... transfer");
        System.out.println("Mimic an account transfer");
    }
}
