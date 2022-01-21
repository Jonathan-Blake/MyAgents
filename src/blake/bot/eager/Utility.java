package blake.bot.eager;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Utility {
    private Utility() {
    }

    public static class Lists {
        private Lists() {
        }

        public static <T> List<T> append(List<T> list, T addition) {
            List<T> ret = new LinkedList<>(list);
            ret.add(addition);
            return ret;

        }

        public static <T> List<T> createFilteredList(Collection<T> fullList, Collection<T> itemsToRemove) {
            List<T> ret = new LinkedList<>(fullList);
            ret.removeAll(itemsToRemove);
            return ret;
        }

        public static <T> List<T> createFilteredList(Collection<T> fullList, T itemToRemove) {
            List<T> ret = new LinkedList<>(fullList);
            ret.remove(itemToRemove);
            return ret;
        }
    }
}
