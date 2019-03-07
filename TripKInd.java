ackage actv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static actv.Utilities.NOT_COMPARABLE;

public class TripKind {
  private ArrayList<Pair> trips; // al posto di questo ci va
  // private ArrayList<Trip> trips;
  private ArrayList<Quadruple> stop_sequence;

  public TripKind(ArrayList<Pair> trips, ArrayList<Quadruple> stop_sequence) {
    this.trips = trips;
    this.stop_sequence = stop_sequence;
  }

  public TripKind() {
    this.trips = new ArrayList<>();
    this.stop_sequence = new ArrayList<>();
  }

  public ArrayList<Quadruple> getStop_sequence() {
    return stop_sequence;
  }

  public ArrayList<Pair> getTrips() {
    return trips;
  }

  public boolean equals(TripKind t) {
    return Utilities.equal(this.stop_sequence, t.stop_sequence);
  }

  public String toStringStopsOnly() {
    StringBuffer result = new StringBuffer();
    int i = stop_sequence.size();
    for (Quadruple quad : stop_sequence) {
      result.append(quad.toString());
      if (i > 0)
        result.append(", ");
      i--;
    }
    return result.toString();
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("Start trip kind for trips: ");
    int i = trips.size();
    for (Pair pair : trips) {
      result.append(pair.toString());
      if (i > 0)
        result.append(", ");
      i--;
    }
    result.append("\n");
    i = stop_sequence.size();
    for (Quadruple quad : stop_sequence) {
      result.append(quad.toString());
      if (i > 0)
        result.append(", ");
      i--;
    }
    result.append("End trip kind\n");
    return result.toString();
  }

  public void merge(TripKind t) {
    for (Pair p : t.trips) {
      this.trips.add(p);
    }
  }

  public TripKind(int trip_id, StopTimes stop_times) {
    this.trips = new ArrayList<>();
    this.stop_sequence = new ArrayList<>();
    int sequence_number = 0;
    boolean finish = false;
    boolean start = false;

    for (int i = 0; i < stop_times.size() && !finish; i++) {
      StopTime current = stop_times.get(i);

      if (current.getTrip_id() == trip_id) {
        if (!start) {// solo alla prima fermata aggiungiamo trip_id e orario di partenza
          trips.add(new Pair(trip_id, current.getDeparture_time()));
          start = true;
        }
        int stop_id = 0;
        Long park_time = null;
        Long trip_time = null;

        stop_id = current.getStop_id();
        park_time = current.getParking_time();
        StopTime next = null;
        if (i < stop_times.size() - 1) {
          next = stop_times.get(i + 1);
          if (current.getTrip_id() == trip_id && next.getTrip_id() != trip_id)
            finish = true;
          if (current.getTrip_id() == trip_id && next.getTrip_id() == trip_id
                  && current.getDeparture_time() != null && next.getArrival_time() != null)
            trip_time = Math.abs(current.getDeparture_time().getTime() - next.getArrival_time().getTime());
        }
        // 20145,06:10:00,06:10:00,1047,1,ASSEGGIANO,0,1,
        stop_sequence.add(new Quadruple(sequence_number, stop_id, park_time, trip_time));
        sequence_number++;
      } else {
        continue;
      }
    }
  }

  public String toJson() {
    StringBuilder b = new StringBuilder();
    b.append('{');
    b.append("\"trips\": [");

    for (Pair pair : trips) {
      b.append(pair.toJson());
      b.append(',');
    }
    b.append("],");
    b.append("\"stop_sequence\": [");
    for (Quadruple quadruple : stop_sequence) {
      b.append(quadruple.toJson());
      b.append(',');
    }
    b.append("]");
    b.append('}');
    return b.toString();
  }

  class Quadruple implements Comparable<Quadruple> {
    int stop_id;
    int sequence_number;
    Long park_time;
    Long trip_time;

    public Quadruple(int stop_id, int sequence_number, Long park_time) {
      this.stop_id = stop_id;
      this.sequence_number = sequence_number;
      this.park_time = park_time;
    }

    public Quadruple(int stop_id, int sequence_number, Long park_time, Long trip_time) {
      this.stop_id = stop_id;
      this.sequence_number = sequence_number;
      this.park_time = park_time;
      this.trip_time = trip_time;
    }

    /**
     * @param q
     * @return -1 se this < o
     * +1 se this > o
     * 0 se this == o
     */
    @Override
    public int compareTo(Quadruple q) {
      // System.out.println("Quadruple.compareTo called");
      if (q.sequence_number > this.sequence_number)
        return -1;
      if (q.sequence_number < this.sequence_number)
        return 1;
      if ((q.park_time != null && this.park_time == null)
              || (q.park_time == null && this.park_time != null)
              || (q.trip_time != null && this.trip_time == null)
              || (q.trip_time == null && this.trip_time != null)
      )
        return NOT_COMPARABLE;
      if (q.stop_id == this.stop_id) {
        if (q.park_time != null && this.park_time != null
                && q.park_time.equals(this.park_time)
                && q.trip_time == null && this.trip_time == null
        ) {
          return 0;
        }
      }
      if (q.stop_id == this.stop_id) {
        if (q.park_time != null && this.park_time != null
                && q.park_time.equals(this.park_time)
                && q.trip_time != null && this.trip_time != null
                && q.trip_time.equals(this.trip_time)) {
          return 0;
        }
      }
      return NOT_COMPARABLE;
    }

    public String toJson() {
      StringBuilder b = new StringBuilder();
      b.append('{');
      b.append("\"stop_id\": " + this.stop_id);
      b.append("\"sequence_number\": " + this.sequence_number);
      b.append("\"park_time\": " + this.park_time);
      b.append("\"trip_time\": " + this.trip_time);
      b.append('}');
      return b.toString();
    }


    public boolean equals(Quadruple t) {
      // System.out.println("Quadruple.equals called");
      return compareTo(t) == 0;
    }

    public String toString() {
      StringBuffer result = new StringBuffer();
      String pt_text = toMinAndSec(park_time);
      String tt_text = toMinAndSec(trip_time);
      result.append("(" + stop_id + ", "
              + sequence_number + ", "
              + pt_text + ", "
              + tt_text + ")");
      return result.toString();
    }

    private String toMinAndSec(Long time) {
      StringBuffer result = new StringBuffer();
      if (time != null) {
        long min = TimeUnit.MILLISECONDS.toMinutes(time);
        long sec = TimeUnit.MILLISECONDS.toSeconds(time);
        result.append(min + ":" + sec);
      } else {
        result.append("null");
      }
      return result.toString();
    }

  }// fine inner class

  // TODO: Pair va eliminata
  // un oggetto trip kind deve contenere un array di trips
  class Pair implements Comparable<Pair> {
    int trip_id;
    Date departure_time;

    public Pair(int stop_id, Date departure_time) {
      this.trip_id = stop_id;
      this.departure_time = departure_time;
    }

    public int getTrip_id() {
      return trip_id;
    }

    public Date getDeparture_time() {
      return departure_time;
    }

    @Override
    public int compareTo(Pair p) {
      // System.out.println("Pair.compareTo called");
      if (p.trip_id == this.trip_id && p.departure_time.equals(this.departure_time))
        return 0;
      return NOT_COMPARABLE;
    }

    public boolean equals(Pair p) {
      // System.out.println("Pair.equals called");
      return compareTo(p) == 0;
    }

    public String toString() {
      String dep_time = departure_time.toString().replaceFirst("(^.)(\\d\\d:\\d\\d:\\d\\d)(.)", "$2");
      return "(" + trip_id + ", " + dep_time + ")";
    }

    public String toJson() {
      StringBuilder b = new StringBuilder();
      b.append('{');
      b.append("\"trip_id\": " + this.trip_id);
      b.append("\"departure_time\": " + "\"" + this.departure_time + "\"");
      b.append('}');
      return b.toString();
    }

  }
  // fine inner class

}// fine classe
