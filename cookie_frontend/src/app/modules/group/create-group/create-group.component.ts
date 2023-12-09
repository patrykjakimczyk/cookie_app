import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { Component } from '@angular/core';
import { GroupService } from '../group.service';

@Component({
  selector: 'app-create-group',
  templateUrl: './create-group.component.html',
  styleUrls: ['./create-group.component.scss'],
})
export class CreateGroupComponent {
  protected createGroupSucceded = false;
  protected groupNameRegex = RegexConstants.groupNameRegex;

  constructor(private groupService: GroupService) {}

  createGroup(name: string) {
    this.groupService.createGroup({ groupName: name }).subscribe({
      next: (_) => {
        this.createGroupSucceded = true;
      },
    });
  }
}
