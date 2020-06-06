import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Movement } from '../../models/movement';
import { TrafficLight } from '../../models/traffic-light';
import { MatPaginator } from '@angular/material/paginator';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';
import { element } from 'protractor';
import { RestService } from 'src/app/services/rest-service';

@Component({
  selector: 'app-infotable',
  templateUrl: './infotable.component.html',
  styleUrls: ['./infotable.component.css']
})
export class InfotableComponent implements OnInit {

  @ViewChild('paginatorOne', {static: true}) movementPaginator: MatPaginator;
  @ViewChild('paginatorTwo', {static: true}) trafficLightPaginator: MatPaginator;

  ws: any;

  movementDisplayedColumns: string[] = ['vin', 'speed', 'longitude', 'latitude', 'crash'];
  movementDataSource = new MatTableDataSource<Movement>();
  trafficLightDisplayedColumns: string[] = ['id', 'longitude', 'latitude', 'status'];
  trafficLightDataSource = new MatTableDataSource<TrafficLight>();
  trafficLights: TrafficLight[] = [];
  

  constructor(private restService: RestService) { 
  }

  ngOnInit(): void {
    this.trafficLightDataSource.paginator = this.trafficLightPaginator;
    this.movementDataSource.paginator = this.movementPaginator;
    this.getAllTrafficLights();
    this.initSocketConnections();
  }

  getAllTrafficLights() {
    this.restService.getAllTrafficLights().subscribe(response => {
      response.forEach(element => {
        const trafficLight = this.createTrafficLight(element);
        this.trafficLights.push(trafficLight);
      })
    });
  }

  createMovement(element: any): Movement {
    return new Movement(element.vin, element.speed, element.longitude, element.latitude, element.crash);
  }

  createTrafficLight(element: any){
    return new TrafficLight(element.id, element.longitude, element.latitude);
  }

  initSocketConnections(){
    let ws = new SockJS("http://localhost:10113/ws");
    this.ws = Stomp.over(ws);
    let that = this;
    this.ws.connect({}, function(frame) {
      that.ws.subscribe("/trafficLights", function(element) {
          let tl = JSON.parse(element.body);
          let trafficLight = that.trafficLights.find(light => tl['trafficLightId'] === light.id);
          trafficLight.statusGreen = tl['green'];
          trafficLight.statusChange = tl['dateTime'];
          const data = that.trafficLightDataSource.data;
          data.unshift(trafficLight);
          that.trafficLightDataSource.data = data;
      });
      that.ws.subscribe("/movements", function(element) {
        let mvm = JSON.parse(element.body);
        const movement = that.createMovement(mvm);
        const data = that.movementDataSource.data;
        data.unshift(movement);
        that.movementDataSource.data = data;     
      });
    })
  } 

}
