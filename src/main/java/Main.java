import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

  private static final double UNIT_ROUNDOFF = Math.ulp(1.0) / 2;

  public static void main(String[] args) {
    {
      String fileNameBE = "polynomTest1BE.dat";
      List<Double> doubleListBE = parseFileToDoubleList(fileNameBE, ByteOrder.BIG_ENDIAN);
      double x = doubleListBE.get(doubleListBE.size() - 1);
      doubleListBE.remove(doubleListBE.size() - 1);
      System.out.println("Big Endian:");
      for (double v : doubleListBE) {
        System.out.println("  " + v);
      }
      System.out.println("x: " + x);
      outputAlgorithmGorner(doubleListBE, x);
      System.out.println();
    }

    {
      String fileNameLE = "polynomTest2LE.dat";
      List<Double> doubleListLE = parseFileToDoubleList(fileNameLE, ByteOrder.LITTLE_ENDIAN);

      double x1 = doubleListLE.get(doubleListLE.size() - 1);
      doubleListLE.remove(doubleListLE.size() - 1);
      double x2 = doubleListLE.get(doubleListLE.size() - 1);
      doubleListLE.remove(doubleListLE.size() - 1);
      double x3 = doubleListLE.get(doubleListLE.size() - 1);
      doubleListLE.remove(doubleListLE.size() - 1);

      System.out.println("Little Endian:");
      for (double v : doubleListLE) {
        System.out.println("  " + v);
      }
      System.out.println("x1: " + x1);
      outputAlgorithmGorner(doubleListLE, x1);
      System.out.println("x2: " + x2);
      outputAlgorithmGorner(doubleListLE, x2);
      System.out.println("x3: " + x3);
      outputAlgorithmGorner(doubleListLE, x3);
      System.out.println();
    }

  }

  private static void outputAlgorithmGorner(List<Double> doubleList, double x) {
    System.out.println("Result: ");
    System.out.println("  Apriori: " + calculateApriori(doubleList, x));
    System.out.println("  Value: " + calculatePolynomialValue(doubleList, x));
//    System.out.println(" SValue: " + calculatePolynomialValueTwo(doubleList, x));
    System.out.println("  Posterior: " + calculatePosterior(doubleList, x));
    System.out.println(
        "The possibility of being zero: " + (Math.abs(calculatePolynomialValue(doubleList, x))
            < calculateApriori(doubleList, x)));
  }

  private static String doubleToBinaryString(double number) {
    long bits = Double.doubleToLongBits(number);
    String binary = Long.toBinaryString(bits);
    binary = String.format("%64s", binary).replace(' ', '0');
    return binary;
  }

  private static double binaryStringToDouble(String binaryString) {
    long bits = Long.parseLong(binaryString, 2);
    return Double.longBitsToDouble(bits);
  }

  private static double parseBitsToDouble(byte[] bytes, ByteOrder byteOrder) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    buffer.order(byteOrder);
    return buffer.getDouble();
  }

  private static List<Double> parseFileToDoubleList(String fileName, ByteOrder byteOrder) {
    List<Double> resultList = new ArrayList<>();
    try (DataInputStream dataInputStream = new DataInputStream(
        Files.newInputStream(Paths.get(fileName)))) {
      byte[] bytes = new byte[8];
      while (dataInputStream.read(bytes) != -1) {
        double v = parseBitsToDouble(bytes, byteOrder);
        resultList.add(v);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return resultList;
  }

  public static double calculateApriori(List<Double> a, double x) {
    double u = Math.ulp(1.0);  // Машинное эпсилон
    double w = 2.0;  // Значение w
    double b = 10.0;  // Значение b

    double sqrtTerm = Math.sqrt(w / b);
    double condition = 1.0 / 2.0 * (sqrtTerm * Math.pow(u, -0.5) - 1);

//    n < 1/2 * (sqrt(w/b) * u^(-1/2) - 1),
//        где:
//    n - степень многочлена
//    w - параметр равный 2, если основание системы счисления (β) четное, и 1 в противном случае
//    b - основание системы счисления (например, 2 или 10)
//    u - машинное эпсилон

    int n = a.size();
    double e = a.get(n - 1);
    if (n < condition)
    {
      for (int i = 1; i < n; i++) {
        e = e * Math.abs(x) + Math.abs(a.get(i));
      }
      e = 2 * (n-1) * UNIT_ROUNDOFF * e;
    }
    else {
      for (int i = 1; i < n; i++) {
        e = e * Math.abs(x) + Math.abs(a.get(i));
      }
      e = 2 * (n - 1) * UNIT_ROUNDOFF / (1 - 2 * (n - 1) * UNIT_ROUNDOFF) * e;
    }

//    Здесь n -1 используется вместо n, потому что в методе Горнера последний коэффициент
//    a_n(где n - степень многочлена) не участвует в итерационном процессе.Последняя итерация в методе
//    Горнера вычисляет значение константы a_0, которая не требует учета в формуле для
//    априорной оценки погрешности.

//    e = 2 * n * UNIT_ROUNDOFF * e;
//    e = 2 * (n) * UNIT_ROUNDOFF / (1 - 2 * (n) * UNIT_ROUNDOFF) * e;
    return e;
  }

  public static double calculatePosterior(List<Double> a, double x) {
    double result = a.get(a.size() - 1);
    double s = Math.abs(a.get(a.size() - 1) / 2);

    for (int i = a.size() - 1; i > 1; i--) {
      result = result * x + a.get(i - 1);
      s = s * Math.abs(x) + Math.abs(result);
    }
    result = result * x + a.get(0);
    double posterior = 2 * (s * Math.abs(x)) + Math.abs(result);
    posterior = UNIT_ROUNDOFF * (posterior / (1 - (2 * a.size() - 1) * UNIT_ROUNDOFF));
    return posterior;
  }

  private static double calculatePolynomialValue(List<Double> a, double x) {
    double result = a.get(a.size() - 1);
    for (int i = a.size() - 1; i > 0; i--) {
      result = result * x + a.get(i - 1);
    }
    return result;
  }

  private static double calculatePolynomialValueTwo(List<Double> a, double x) {
    double result = 0;
    for (int i = 0; i < a.size(); i++) {
      result += Math.pow(x, i) * a.get(i);
    }
    return result;
  }


}