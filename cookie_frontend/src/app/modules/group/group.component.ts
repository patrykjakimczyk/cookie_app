import { Component, OnInit } from '@angular/core';

import { GroupDTO } from 'src/app/shared/model/types/group-types';
import { GroupService } from './group.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-group',
  templateUrl: './group.component.html',
  styleUrls: ['./group.component.scss'],
})
export class GroupComponent implements OnInit {
  protected userGroups: GroupDTO[] = [];

  constructor(private groupService: GroupService, private router: Router) {}

  ngOnInit(): void {
    this.groupService.getUserGroups().subscribe({
      next: (response) => {
        this.userGroups = response.userGroups;
      },
    });
  }

  goToPantry(pantryId: number) {
    this.router.navigate(['/pantries/' + pantryId]);
  }

  goToGroup(groupId: number) {
    this.router.navigate(['/groups/' + groupId]);
  }
}
