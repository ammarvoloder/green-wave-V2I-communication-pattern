import { Component, OnInit } from '@angular/core';
import { TrafficLight } from 'src/app/models/traffic-light';
import { RestService } from 'src/app/services/rest-service';
import { element } from 'protractor';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit {

  center: google.maps.LatLng;
  coordinates = new google.maps.LatLng(48.16411, 16.34629);
  trafficLights: TrafficLight[];
  markers: google.maps.Marker[];

  redLight = 'src/assets/images/red.png';
  
  constructor(private restService: RestService) { }

  ngOnInit(): void {
    this.markers = []
    this.center = this.coordinates;
    this.getAllTrafficLights();
  }

  getAllTrafficLights(){
    this.restService.getAllTrafficLights().subscribe(response => {
      this.trafficLights = response;
      this.trafficLights.forEach(element => {
        var coordinates = new google.maps.LatLng(element.latitude, element.longitude);
        const marker = new google.maps.Marker;
        const options = {
          icon: this.redLight
        }
        marker.setPosition(coordinates);
        marker.setOptions(options);
        this.markers.push(marker);
      })
    })
  }

}
