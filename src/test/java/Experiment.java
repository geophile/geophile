public class Experiment
{
    public static void main(String[] args)
    {
        round(1.6);
        round(1.5);
        round(1.4);
        round(-1.6);
        round(-1.5);
        round(-1.4);
        truncate(1.6);
        truncate(1.5);
        truncate(1.4);
        truncate(-1.6);
        truncate(-1.5);
        truncate(-1.4);
    }

    private static void round(double x)
    {
        long y = Math.round(x);
        print("round(%s): %s", x, y);
    }

    private static void truncate(double x)
    {
        long y = (long) x;
        print("truncate(%s): %s", x, y);
    }

    private static void print(String template, Object ... args)
    {
        System.out.println(String.format(template, args));
    }
}