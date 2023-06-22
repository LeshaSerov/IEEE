import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Convert {


  // Функция для парсинга байтовых символов в список чисел формата IEEE-754 binary64 (big-endian)
  private static double[] parseBitsBigEndian(byte[] symbols) {
    ByteBuffer buffer = ByteBuffer.wrap(symbols);
    buffer.order(ByteOrder.BIG_ENDIAN);
    double[] bitsArray = new double[symbols.length / 8];
    for (int i = 0; i < symbols.length; i += 8) {
      bitsArray[i / 8] = buffer.getDouble(i);
    }
    return bitsArray;
  }

  // Функция для парсинга байтовых символов в список чисел формата IEEE-754 binary64 (little-endian)
  private static double[] parseBitsLittleEndian(byte[] symbols) {
    ByteBuffer buffer = ByteBuffer.wrap(symbols);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    double[] bitsArray = new double[symbols.length / 8];
    for (int i = 0; i < symbols.length; i += 8) {
      bitsArray[i / 8] = buffer.getDouble(i);
    }
    return bitsArray;
  }



  public static void main(String[] args) {
    String filePath = "polynomTest1BE.dat";
//    double[] bitsArray = parseBitsFromFile(filePath);

    // Далее можно использовать bitsArray в вашем коде
    // ...
  }

}
