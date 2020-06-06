export class TrafficLight {
    id: number;
    longitude: number;
    latitude: number;
    statusGreen: boolean;
    statusChange: Date;

    constructor(id: number, longitude: number, latitude: number){
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    fullInfo(){
        let status = this.statusGreen ? 'GREEN' : 'RED';
        return 'ID: ' + this.id + '\n'
                + 'Longitude: ' + this.longitude + '\n'
                + 'Latitude: ' + this.latitude + '\n'
                + 'Status: ' + status + '\n'
                + 'Next status change: ' + this.statusChange;
    }
}
