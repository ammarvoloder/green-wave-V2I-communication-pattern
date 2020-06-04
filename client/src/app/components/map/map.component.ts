import { Component, OnInit, ViewChild, NgZone, ElementRef, AfterViewInit } from '@angular/core';
import { TrafficLight } from 'src/app/models/traffic-light';
import { RestService } from 'src/app/services/rest-service';
import { element } from 'protractor';
import { MapMarker, MapInfoWindow, GoogleMap } from '@angular/google-maps';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements AfterViewInit {
  @ViewChild('mapContainer', {static: false}) gmap: ElementRef;
  
  map: google.maps.Map;
  coordinates = new google.maps.LatLng(48.16411, 16.34629);

  mapOptions: google.maps.MapOptions = {
    center: this.coordinates,
    zoom: 14.5
   };



  trafficLights: TrafficLight[];
  markers: google.maps.Marker[];
  //options: google.maps.MarkerOptions[];
  ws: any;
  redLight = 'assets/images/red.png';
  greenLight = 'assets/images/green.png';
  infoContent: string;
  trafficLightMap: Map<Number, google.maps.Marker>;

  
  constructor(private restService: RestService) { 
    this.trafficLightMap = new Map();
    this.trafficLights = [];
  }

  ngAfterViewInit(): void {
    this.mapInitializer();
    this.getAllTrafficLights();
    //this.initSocketConnections();
  }

  mapInitializer(){
    this.map = new google.maps.Map(this.gmap.nativeElement, 
      this.mapOptions);
  }

  getAllTrafficLights(){
    this.restService.getAllTrafficLights().subscribe(response => {
      response.forEach(element => {
        var coordinates = new google.maps.LatLng(element.latitude, element.longitude);
        const marker = new google.maps.Marker;
        const trafficLight = this.createTrafficLight(element);
        marker.setPosition(coordinates);
        marker.setIcon(this.redLight);
        marker.setMap(this.map);
        var content = `<span style="white-space: pre;">${trafficLight.fullInfo()}</span>`
        const infoWindow = new google.maps.InfoWindow;
        infoWindow.setContent(content);
        marker.addListener('click', () => {
          infoWindow.open(marker.getMap(), marker);
          setTimeout(function(){infoWindow.close();}, 2000);
        });
        //this.markers.push(marker);
        //this.trafficLights.push(new TrafficLight(element.id, element.longitude, element.latitude));
        //this.trafficLightMap.set(element.id, marker);
      })
    });
  }

  createTrafficLight(element: any): TrafficLight {
    return new TrafficLight(element.id, element.longitude, element.latitude);
  }

/* 
  openInfo(marker: MapMarker) {
    let id;
    for (let [key, value] of this.trafficLightMap.entries()) {
      if(value.getPosition() ==  marker.getPosition()){
        id = key;
      }
    }
    let trafficLight = this.trafficLights.find(light => light.id === id);
    console.log(trafficLight);
    this.infoContent = trafficLight.fullInfo();
    this.infoWindow.open(marker);
  }

  

  initSocketConnections(){
    let ws = new SockJS("http://localhost:10113/ws");
    this.ws = Stomp.over(ws);
    let that = this;
    this.ws.connect({}, function(frame) {
      that.ws.subscribe("/trafficLights", function(element) {
          console.log(element.body);
          let tl = JSON.parse(element.body);
          let trafficLight = that.trafficLights.find(light => tl['trafficLightId'] === light.id);
          let index = that.trafficLights.indexOf(trafficLight);
          trafficLight.statusGreen = tl['green'];
          that.trafficLights[index] = trafficLight;
          let marker = that.trafficLightMap.get(trafficLight.id); 
          let marker_index = that.markers.indexOf(marker);
          /* if (trafficLight.statusGreen) {
            that.options = {
              icon: that.greenLight
            }
          } else {
            that.options = {
              icon: that.redLight
            } 
          }
      });
    })
  } */

}
