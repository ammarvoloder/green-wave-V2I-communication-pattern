import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router'
import { GoogleMapsModule } from '@angular/google-maps'


import { AppComponent } from './app.component';
import { MapComponent } from './components/map/map.component';

const routes: Routes = [
  { path: '', component: MapComponent }
]
@NgModule({
  declarations: [
    AppComponent,
    MapComponent
  ],
  imports: [
    BrowserModule,
    RouterModule.forRoot(routes),
    GoogleMapsModule
  ],
  exports: [
    RouterModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
