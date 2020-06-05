export class Movement {
    vid: string;
    speed: number;
    longitude: number;
    latitude: number;
    dateTime: Date;
    crash: boolean;

    constructor(vid: string, speed: number, longitude: number, latitude: number, crash: boolean){
        this.vid = vid;
        this.speed = speed;
        this.longitude = longitude;
        this.latitude = latitude;
        this.crash = crash;
    }
}
