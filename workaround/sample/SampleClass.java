package sample;

@SuppressWarnings("unused")
public final class SampleClass {

    @SuppressWarnings("StringOperationCanBeSimplified")  // Disable inlining
    public static final String MESSAGE = "hello, world".toString();

    public static void main(String[] args) {
        System.out.println(MESSAGE);
    }
}
