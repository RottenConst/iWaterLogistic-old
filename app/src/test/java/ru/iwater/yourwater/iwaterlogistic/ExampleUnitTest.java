package ru.iwater.yourwater.iwaterlogistic;

import org.junit.Before;
import org.junit.Test;

import ru.iwater.yourwater.iwaterlogistic.utils.Helper;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Before
    public void setUp() throws Exception {
         //создание объекта класса Helper
    }

    @Test
    public void formatedDate_isCorrect() {
        assertEquals(Helper.returnFormatedDate(0),"2019-06-02");
        assertEquals(Helper.returnFormatedDate(1),"2019-06-02");
    }
}