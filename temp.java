import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class temp implements Serializable {

//    public final void writeObject(Object o) throws IOException;
//
//    public final Object readObject()
//            throws IOException, ClassNotFoundException;

    public class Person implements Serializable {
//        private static final long serialVersionUID = 1L;
//        static String country = "ITALY";
        private int age;
        private String name;
        transient int height;

        public void setAge(int i) {
            age = i;
        }

        public void setName(String joe) {
            name = joe;
        }

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        // getters and setters
    }
    public void whenSerializingAndDeserializing_ThenObjectIsTheSame() throws IOException, ClassNotFoundException {
        Person person = new Person();
        person.setAge(20);
        person.setName("Joe");

        FileOutputStream fileOutputStream
                = new FileOutputStream("yourfile.txt");
        ObjectOutputStream objectOutputStream
                = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(person);
        objectOutputStream.flush();
        objectOutputStream.close();

        FileInputStream fileInputStream
                = new FileInputStream("yourfile.txt");
        ObjectInputStream objectInputStream
                = new ObjectInputStream(fileInputStream);
        Person p2 = (Person) objectInputStream.readObject();
        objectInputStream.close();

        System.out.println(p2.getAge() == person.getAge());

        p2.setAge(50);
        System.out.println(p2.getAge());
        System.out.println(person.getAge());

        System.out.println(p2.getAge() == person.getAge());
        System.out.println(p2.getName().equals(person.getName()));
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        temp person1 = new temp();
        person1.whenSerializingAndDeserializing_ThenObjectIsTheSame();
        String[] cardActions = {
                "get,yellow:ask,Remy La Rocque,",
                "get,:viewDeck",
                "get,red:ask,Nadia Bwalya,",
                "get,green:ask,Lily Nesbit,",
                "viewDeck:ask,Buford Barnswallow,",
                "get,red:ask,Earl of Volesworthy,",
                "get,:ask,Nadia Bwalya,",
                "get,green:ask,Stefano Laconi,",
                "get,yellow:viewDeck",
                "get,:ask,Dr. Ashraf Najem,",
                "get,green:viewDeck",
                "get,red:viewDeck",
                "get,:ask,Mildred Wellington,",
                "get,:move,",
                "get,:ask,Earl of Volesworthy,",
                "get,:ask,Remy La Rocque,",
                "viewDeck:ask,Viola Chung,",
                "get,:ask,Stefano Laconi,",
                "get,:ask,Viola Chung,",
                "get,:viewDeck",
                "get,:ask,Lily Nesbit,",
                "get,yellow:ask,Mildred Wellington,",
                "get,:ask,Buford Barnswallow,",
                "get,:move,",
                "move,:ask,Dr. Ashraf Najem,",
                "get,:viewDeck",
                "get,:ask,Trudie Mudge,",
                "move,:ask,Trudie Mudge,"
        };

        String[] cardsRemaining = cardActions.clone();
        cardsRemaining[1] = "fuckthisshit";
        System.out.println(cardActions[1]);
        System.out.println(cardsRemaining[1]);


    }
}
