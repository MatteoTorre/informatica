package actv;

import java.util.ArrayList;

public class CompressedTimeTable {
  private int route_id;// al posto di questo ci va
  // private Route route;  la classe Route deve estendere comparable
  // e gli oggetti di tipo Route si ordinano linearmente rispetto al valore del route_id
  private ArrayList<TripKind> trip_kinds;

  // bisogna aggiungere la funzionalità che ricostruisce un oggetto di tipo CompressedTimeTable
  // a partire da un file (non necessariamente di testo) in cui esso viene "trascritto"

  public CompressedTimeTable(int route_id) {
    this.route_id = route_id;
    this.trip_kinds = new ArrayList<>();
    System.out.println("Ready to compress the timetable for route " + route_id);
  }

  public int getRoute_id() {
    return route_id;
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("Start compressed time table for route " + route_id + "\n");
    for (TripKind tk : trip_kinds){
      result.append(tk.toString());
    }
    result.append("End compressed time table for route " + route_id + "\n");
    return result.toString();
  }

  public void add(TripKind tk){
    TripKind old_tk = get(tk);
    if (old_tk != null){
      old_tk.merge(tk);
      //System.out.println("Two trips merged in the same trip kind.");
    } else {
      trip_kinds.add(tk);
      //System.out.println("New trip kind found.");
    }
  }

  public TripKind get(TripKind tk){
    TripKind ris = null;
    for (TripKind element : trip_kinds){
      if (element.equals(tk))
        return element;
    }
    return ris;
  }

  public String toJson()
  {
    StringBuilder b= new StringBuilder();
    b.append('{');
    b.append("\"route_id\":"+this.route_id);
    b.append("\"trip_kinds\":[");
    for(TripKind t: trip_kinds){
      t.toJson();
    }
    b.append('}');
    return b.toString();
  }
} // fine classe
