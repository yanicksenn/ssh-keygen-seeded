import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        var argsList = Arrays.asList(args);
        var commands = argsList.stream()
                .filter(a -> !a.startsWith("-"))
                .map(a -> a.toLowerCase().trim())
                .toList();

        var parameters = argsList.stream()
                .filter(a -> a.startsWith("--") && a.contains("="))
                .map(a -> a.substring(2).split("="))
                .collect(Collectors.toUnmodifiableMap(
                        a -> a[0].toLowerCase().trim(),
                        a -> a[1].trim()
                ));

        var flags = argsList.stream()
                .filter(a -> a.startsWith("-") && !a.contains("="))
                .map(a -> a.replace("-", " "))
                .map(a -> a.toLowerCase().trim())
                .map(a -> a.replace(" ", "-"))
                .collect(Collectors.toUnmodifiableSet());

        try {
            var isHelp = isHelp(flags);
            if (isHelp) {
                printUsage(System.out);

            } else {
                String command = getCommand(commands);
                String seed = getSeed(parameters);
                String passphrase = getPassphrase(parameters);
                int keySize = getKeySize(parameters);
                var isVerbose = isVerbose(flags);

                switch (command) {
                    case "rsa" -> new RSAKeyPairGenerator(new RSAKeyPairParams(seed, passphrase, keySize, isVerbose)).run();
                    default -> throw new IllegalArgumentException("command %s is unknown".formatted(command));
                }
            }

        } catch (Throwable e) {
            System.err.printf("ERROR: %s%n", e.getMessage());
            System.err.println();
            printUsage(System.err);
            System.exit(1);
        }
    }

    private static String getCommand(List<String> commands) {
        if (commands.size() != 1) {
            throw new IllegalArgumentException("command must be provided");
        }
        return commands.get(0);
    }

    private static String getSeed(Map<String, String> parameters) {
        var seed = parameters.get("seed");
        if (seed == null) {
            throw new IllegalArgumentException("seed must be provided");
        }
        return seed;
    }

    private static String getPassphrase(Map<String, String> parameters) {
        var passphrase = parameters.get("passphrase");
        if (passphrase == null) {
            throw new IllegalArgumentException("passphrase must be provided");
        }
        return passphrase;
    }

    private static int getKeySize(Map<String, String> parameters) {
        try {
            var keySizeString = parameters.getOrDefault("key-size", "4096");
            return Integer.parseInt(keySizeString);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("key-size must be a number");
        }
    }

    private static boolean isVerbose(Set<String> flags) {
        return flags.contains("v") || flags.contains("verbose");
    }

    private static boolean isHelp(Set<String> flags) {
        return flags.contains("h") || flags.contains("help");
    }

    private static void printUsage(PrintStream printStream) {
        printStream.println("Usage: ssh-keygen-seeded <COMMAND> [FLAGS] --seed=<SEED> --passphrase=<PASSPHRASE>");
        printStream.println();
        printStream.println("Commands:");
        printStream.println("\trsa\t\tGenerating RSA key pair");
        printStream.println();
        printStream.println("Flags:");
        printStream.println("\t-v, --verbose\tMake the operation more talkative");
        printStream.println("\t-h, --help\tGet help for this tool");
    }
}
