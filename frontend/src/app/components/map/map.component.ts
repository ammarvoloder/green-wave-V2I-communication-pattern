import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { TrafficLight } from '../../models/trafficlight';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit {

  @ViewChild('mapContainer', { }) gmap: ElementRef;
  map: google.maps.Map;

  coordinates = new google.maps.LatLng(48.16411, 16.34629);
  center: google.maps.LatLng;
  mapOptions: google.maps.MapOptions;
  startingSetup: any[];
  overlays: any[];
  trafficLights: Array<TrafficLight>
  ws: any;

  constructor() { }

  ngOnInit() {
   this.center = this.coordinates;
  }
 
  mapInitializer() {
    this.map = new google.maps.Map(this.gmap.nativeElement, 
    this.mapOptions);
    this.startingSetup = [
      new google.maps.Circle({center: new google.maps.LatLng(48.17663, 16.35614),
        fillColor: '#1976D2', fillOpacity: 0.35, strokeWeight: 1, radius: 1000}),
      new google.maps.Marker({position: new google.maps.LatLng(48.17663, 16.35614),
        icon: 'http://maps.google.com/mapfiles/kml/pal4/icon62.png'})
    ];
  }

  connectToSocket(){
    var socket = new SockJS('http://localhost:10113/ws');
    this.ws = Stomp.over(socket);
    let that = this;
    this.ws.connect({}, function(frame) {
      that.ws.subscribe("/topic", function(message) {
        console.log(message)
        console.log(message.body);
      });
  });
}
  setupTrafficLights(){
    
  }

  private refreshMap() {
    const positions = [];
    if (this.startingSetup) {
      this.startingSetup.forEach((value) => {
        positions.push(value);
      });
    }
  
    this.overlays = positions;
  }
}
