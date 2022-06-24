package org.youzipi.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author wuqiantai
 */
class KollectionUtilTest {

    class Foo {
        public Foo(int bar) {
            this.bar = bar;
        }

        private int bar;

        public int getBar() {
            return bar;
        }
    }

    @Test
    public void distinctBy() {
        List<Foo> list = new ArrayList<>();
        list.add(new Foo(1));
        list.add(new Foo(1));
        list.add(new Foo(2));
        list.add(new Foo(2));
        list.add(new Foo(3));

        List<Foo> distinctList = list.stream()
                .filter(KollectionUtil.distinctBy(Foo::getBar))
                .collect(Collectors.toList());
        Assertions.assertEquals(3, distinctList.size());

        Assertions.assertEquals(1, distinctList.get(0).getBar());
        Assertions.assertEquals(2, distinctList.get(1).getBar());
        Assertions.assertEquals(3, distinctList.get(2).getBar());
    }
}