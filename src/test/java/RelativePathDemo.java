import java.nio.file.Path;
import java.nio.file.Paths;

public class RelativePathDemo {
    public static void main(String[] args) {
        Path path1 = Paths.get("/opt", "a", "b", "c");
        Path path2 = Paths.get("/opt", "a");
        Path path3 = Paths.get("/opt", "a", "e", "d");
        System.out.println(path1.relativize(path2)); // ../.. ( go back twice )
        System.out.println(path2.relativize(path1)); // b/c ( go forward to b and c ) for path2 to be equal to path1
        System.out.println("------------------------");
        System.out.println(path1.relativize(path3)); // ../../e/d
        System.out.println(path3.relativize(path1)); // ../../b/c
    }
}
