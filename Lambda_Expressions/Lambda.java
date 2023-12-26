import java.util.ArrayList;
import java.util.List;

public class Lambda {
    public static void main(String[] argv) { 
        List<Integer> a = new ArrayList<Integer>();
        for(int i=1; i<=5; i++)
            a.add(i);
        stampa(a);

        // se noi il metodo doppio lo dobbiamo utilizzare solo una volta
        // non ci interessa come si chiama, ci basterebbe passare alla 
        // funzione solamente l'input e specificare l'output
        // utilizziamo quindi le lambda expression, cioè
        // una funzione con parametro e restituisce il doppio di quel parametro
        // essenzialmente una funzione senza nome (anonime).
        List<Integer> b = map(a, n -> 2*n); // lambda expression 
        stampa(b);

        List<Integer> c = map(a, Lambda::triplo);
        stampa(c);

        List<Integer> d = map(a, n -> n * n); // lambda expression
        stampa(d);
    }


    static Integer triplo(Integer n){
        return 3*n;
    }

    // abbiamo reso la funzione generica con <T1, T2>
    private static<T1,T2> List<T2> map(List<T1> a, Funzione<T1,T2> g) {
        List<T2> b;
        b = new ArrayList<T2>();
        for(T1 e: a)
            b.add(g.applica(e));
        return b;
    }

    private static void stampa(List<Integer> a) {
        a.forEach(e -> {
            System.out.print(e);
            System.out.print(" ");
        });
        System.out.println();
    }
}