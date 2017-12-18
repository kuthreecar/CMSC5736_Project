package cuhk.cse.cmsc5736project.models;

public class Beacon
{
    private String uuid;
    private int major;
    private int minor;
    private int rssi;
    private double path_loss_exponent;
    double one_meter_power = 0;
    double pos_x = 0,pos_y = 0;

    public boolean isSameBeacon(Beacon b)
    {
        return uuid.equals(b.uuid) && major == b.major && minor == b.minor;
    }
    public void setUUID(String uuid)
    {
        this.uuid = uuid;
    }
    public void setMajor(int major)
    {
        this.major = major;
    }
    public void setMinor(int minor)
    {
        this.minor = minor;
    }
    public void setRSSI(int rssi)
    {
        this.rssi = rssi;
    }
    public void setPathLossExponent(double path_loss_exponent)
    {
        this.path_loss_exponent = path_loss_exponent;
    }
    public void setOneMeterPower(double one_meter_power)
    {
        this.one_meter_power = one_meter_power;
    }
    public void setPos(double x,double y)
    {
        this.pos_x = x;
        this.pos_y = y;
    }

    public String getUUID()
    {
        return uuid;
    }

    public int getMinor(){
        return minor;
    }
    public int getMajor()
    {
        return major;
    }
    public int getRSSI()
    {
        return rssi;
    }

    public double calDistance(int power)
    {
        return Math.pow((one_meter_power/power),(1/one_meter_power));
    }
}
