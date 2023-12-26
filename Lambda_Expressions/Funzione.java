/*
 * Le interfacce che hanno un solo metodo sono dette "interfacce funzionali"
 */

public interface Funzione<T1,T2> {
    T2 applica(T1 n);
}
