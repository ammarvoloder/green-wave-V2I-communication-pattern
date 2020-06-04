import { Component, OnInit, ViewChild } from '@angular/core';
import { TrafficLight } from 'src/app/models/traffic-light';
import { RestService } from 'src/app/services/rest-service';
import { element } from 'protractor';
import { MapMarker, MapInfoWindow } from '@angular/google-maps';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

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
  ws: any;
  redLight = 'assets/images/green.png';
  infoContent: string;
  trafficLightMap: Map<Number, google.maps.Marker>;

  
  constructor(private restService: RestService) { 
    this.trafficLightMap = new Map()
  }

  ngOnInit(): void {
    this.trafficLights = []
    this.markers = []
    this.center = this.coordinates;
    this.getAllTrafficLights();
    this.initSocketConnections();
  }

  openInfo(marker: MapMarker) {
    let id;
    for (let [key, value] of this.trafficLightMap.entries()) {
      if(value.getPosition() ==  marker.getPosition()){
        id = key
      }
    }
    let trafficLight = this.trafficLights.find(light => light.id === id)
    this.infoContent = trafficLight.fullInfo()
    this.infoWindow.open(marker);
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

  initSocketConnections(){
    let ws = new SockJS("http://localhost:10113/ws");
    this.ws = Stomp.over(ws);
    let that = this;
    this.ws.connect({}, function(frame) {
      that.ws.subscribe("/trafficLights", function(element) {
          console.log(element.body);
          console.log(element.body.id);
      });
    })
  }

}
