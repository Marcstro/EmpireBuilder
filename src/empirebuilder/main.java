
package empirebuilder;

public class main {

    public static void main(String[] args) {
        GameManager gameManager = new GameManager();
    }

    // Alternative GC: -XX:+UseZGC
    // count lines: git ls-files | grep '\.java' | xargs wc -l
}
