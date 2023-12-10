import { ActivatedRoute, Router } from '@angular/router';
import { GroupService } from './../group.service';
import { Component, Input, OnInit } from '@angular/core';
import { GroupDetailsDTO } from 'src/app/shared/model/types/group-types';

@Component({
  selector: 'app-group-details',
  templateUrl: './group-details.component.html',
  styleUrls: ['./group-details.component.scss'],
})
export class GroupDetailsComponent implements OnInit {
  @Input({ required: true }) groupId!: number;
  protected group: GroupDetailsDTO | null = null;

  constructor(
    private groupService: GroupService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.groupId = this.route.snapshot.params['id'];

    this.groupService.getGroup(this.groupId).subscribe({
      next: (response) => {
        this.group = response;
        console.log(this.group);
      },
      error: (_) => {
        this.router.navigate(['/']);
      },
    });
  }
}
