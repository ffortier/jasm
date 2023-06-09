package jasm.common;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * Inspired by:
 * https://sormuras.github.io/blog/2020-05-06-records-to-text-block.html
 */
public class Records {
    /** Returns a multi-line string representation of the given object. */
    public static String toTextBlock(Record record) {
        return toTextBlock(0, record, "\t");
    }

    /** Returns a multi-line string representation of the given object. */
    private static String toTextBlock(int level, Record record, String indent) {
        var lines = new ArrayList<String>();
        if (level == 0)
            lines.add(record.getClass().getSimpleName());

        var components = record.getClass().getRecordComponents();
        Arrays.sort(components, Comparator.comparing(RecordComponent::getName));

        for (var component : components) {
            final var name = component.getName();
            final var shift = indent.repeat(level);
            try {
                final var value = component.getAccessor().invoke(record);
                final var nested = value.getClass();

                if (nested.isRecord()) {
                    lines.add(String.format("%s%s%s -> %s", shift, indent, name, nested.getSimpleName()));
                    lines.add(toTextBlock(level + 2, (Record) value, indent));
                    continue;
                }

                if (value instanceof Collection<?> collection) {
                    final var it = collection.iterator();

                    if (it.hasNext()) {
                        final var item = it.next();

                        if (item != null && item.getClass().isRecord()) {
                            lines.add(String.format("%s%s%s = [", shift, indent, name));
                            lines.add(String.format("%s%s -> %s", shift, indent, item.getClass().getSimpleName()));
                            lines.add(toTextBlock(level + 2, (Record) item, indent));

                            while (it.hasNext()) {
                                final var next = it.next();
                                lines.add(String.format("%s%s -> %s", shift, indent, next.getClass().getSimpleName()));
                                lines.add(toTextBlock(level + 2, (Record) next, indent));
                            }
                            lines.add(String.format("%s%s]", shift, indent));

                            continue;
                        }
                    }
                }
                lines.add(String.format("%s%s%s = %s", shift, indent, name, value));
            } catch (ReflectiveOperationException e) {
                lines.add("// Reflection over " + component + " failed: " + e);
            }
        }
        return String.join(System.lineSeparator(), lines);
    }
}
