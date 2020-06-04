import { Component, OnInit, ViewChild } from '@angular/core';
import { TrafficLight } from 'src/app/models/traffic-light';
import { RestService } from 'src/app/services/rest-service';
import { element } from 'protractor';
import { MapMarker, MapInfoWindow } from '@angular/google-maps';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit {

  @ViewChild(MapInfoWindow, { static: false }) infoWindow: MapInfoWindow;
  center: google.maps.LatLng;
  coordinates = new google.maps.LatLng(48.16411, 16.34629);
  trafficLights: TrafficLight[];
  markers: google.maps.Marker[];
  options: google.maps.MarkerOptions;

  


  redLight = 'assets/images/green.png';
  infoContent = '';
  
  constructor(private restService: RestService) { }

  ngOnInit(): void {
    this.markers = []
    this.center = this.coordinates;
    this.getAllTrafficLights();
    //this.initSocketConnections();
  }

  openInfo(marker: MapMarker) {
    this.infoContent = "Bojana Kecman";
    this.infoWindow.open(marker);
  }

  getAllTrafficLights(){
    this.restService.getAllTrafficLights().subscribe(response => {
      this.trafficLights = response;
      console.log(this.trafficLights);
      this.trafficLights.forEach(element => {
        var coordinates = new google.maps.LatLng(element.latitude, element.longitude);
        var marker = new google.maps.Marker;
        this.options = {
          icon: this.redLight, 
        }
        marker.setPosition(coordinates);
        this.markers.push(marker);
      })
    })
  }

}
