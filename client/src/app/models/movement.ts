import { Vehicle } from './vehicle';

export class Movement {
    vin: string;
    speed: number;
    longitude: number;
    latitude: number;
    dateTime: Date;
    crash: boolean;
    vehicle: Vehicle;

    constructor(vin: string, speed: number, longitude: number, latitude: number, crash: boolean){
        this.vin = vin;
        this.speed = speed;
        this.longitude = longitude;
        this.latitude = latitude;
        this.crash = crash;
    }

    fullInfo(){
        return 'VIN: ' + this.vin + '\n'
                + 'Model: ' + this.vehicle.model + '\n'
                + 'Producer: ' + this.vehicle.producer + '\n'
                + 'Speed: ' + this.speed + 'km/h \n'
                + 'Longitude: ' + this.longitude + '\n'
                + 'Latitude: ' + this.latitude + '\n'
                + 'Crash happened: ' + this.crash + '\n'
                
    }
}
