package examples.data;

import java.util.Random;

public class Data {
    private int id = new Random().nextInt(900)+100;
    private  String name ="test";
    private  String phone = "18074546423";
    private  String address ="Beijing1";


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
