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
    // Пример использования функций
    String fileNameBE = "polynomTest1BE.dat";
    String fileNameLE = "polynomTest2LE.dat";

    List<Double> doubleListBE = parseFileToDoubleList(fileNameBE, ByteOrder.BIG_ENDIAN);
    List<Double> doubleListLE = parseFileToDoubleList(fileNameLE, ByteOrder.LITTLE_ENDIAN);

    System.out.println("Big-Endian:");
    for (double v : doubleListBE) {
      System.out.println(v);
    }

    System.out.println("\nLittle-Endian:");
    for (double v : doubleListLE) {
      System.out.println(v);
    }

    double arg3 = doubleListLE.get(doubleListLE.size() - 1);
    doubleListLE.remove(doubleListLE.size() - 1);

    double arg2 = doubleListLE.get(doubleListLE.size() - 1);
    doubleListLE.remove(doubleListLE.size() - 1);

    double arg1 = doubleListLE.get(doubleListLE.size() - 1);
    doubleListLE.remove(doubleListLE.size() - 1);

    System.out.println(calculatePolynomialValue(doubleListLE, arg1));
    System.out.println(calculatePosterior(doubleListLE, arg1));
    System.out.println(calculateApriori(doubleListLE, arg1));

  }

  //Преобразует значение типа double в его бинарное представление в формате IEEE 754 с длиной 64 бита.
  private static String doubleToBinaryString(double number) {
    long bits = Double.doubleToLongBits(number);
    String binary = Long.toBinaryString(bits);
    binary = String.format("%64s", binary).replace(' ', '0');
    return binary;
  }

  //Преобразует строку, содержащую бинарное представление значения типа double в формате IEEE 754 с длиной 64 бита,
  private static double binaryStringToDouble(String binaryString) {
    long bits = Long.parseLong(binaryString, 2);
    return Double.longBitsToDouble(bits);
  }

  // Функция для преобразования массива битов в double
  private static double parseBitsToDouble(byte[] bytes, ByteOrder byteOrder) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    buffer.order(byteOrder);
    return buffer.getDouble();
  }

  // Функция для чтения файла и преобразования строки битов в double (Big-Endian)
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
    int n = a.size();
    double e = a.get(n - 1);
    for (int i = 1; i < n; i++) {
      e = e * Math.abs(x) + Math.abs(a.get(i));
    }
    e = 2 * (n - 1) * UNIT_ROUNDOFF / (1 - 2 * (n - 1) * UNIT_ROUNDOFF) * e;
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

}