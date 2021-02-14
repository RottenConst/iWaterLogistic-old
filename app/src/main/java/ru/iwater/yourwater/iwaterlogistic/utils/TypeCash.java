package ru.iwater.yourwater.iwaterlogistic.utils;

public enum TypeCash {

    CASH("Наличные") {
        @Override
        public float getCash(float cash) {
            return cash;
        }
    },
    NON_CASH("Без наличные") {
        @Override
        public float getCash(float cash) {
            return cash;
        }
    },
    ON_SiTE("На сайте") {
        @Override
        public float getCash(float cash) {
            return cash;
        }
    },
    ON_TERMINAL("Оплата через терминал") {
        @Override
        public float getCash(float cash) {
            return cash;
        }
    },
    TRANSFER("Оплата переводом") {
        @Override
        public float getCash(float cash) {
            return cash;
        }
    };

    private final String title;

    TypeCash(String title) {
        this.title = title;
    }

    public abstract float getCash(float cash);

    public String getTitle() {
        return title;
    }
}
